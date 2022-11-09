package com.mfexpress.rent.deliver.scheduler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mfexpress.common.app.userCentre.dto.qry.EmployeeListByOrgAndDutyQry;
import com.mfexpress.common.domain.api.UserAggregateRootApi;
import com.mfexpress.common.domain.dto.UserDTO;
import com.mfexpress.component.dto.wx.cp.WxCpSendMessageDTO;
import com.mfexpress.component.dto.wx.cp.WxMessageTypeEnum;
import com.mfexpress.component.response.Result;
import com.mfexpress.component.starter.tools.wx.MFWxWorkTools;
import com.mfexpress.component.utils.util.ResultDataUtils;
import com.mfexpress.rent.deliver.config.DeliverProjectProperties;
import com.mfexpress.rent.deliver.constant.ServeEnum;
import com.mfexpress.rent.deliver.domainapi.ServeAggregateRootApi;
import com.mfexpress.rent.deliver.dto.data.ContractExpireNotifyDTO;
import com.mfexpress.rent.deliver.dto.data.NoticeTemplateInfoDTO;
import com.mfexpress.rent.deliver.dto.data.serve.dto.ContractWillExpireInfoDTO;
import com.mfexpress.rent.deliver.dto.data.serve.qry.ContractWillExpireQry;
import com.mfexpress.transportation.customer.api.CustomerAggregateRootApi;
import com.mfexpress.transportation.customer.dto.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * "租赁中”、“维修中",n天后过期的服务单
 * <p>
 * 根据车辆所在客户所属管理区对所属管理区下的不同职责进行提醒
 * 1. 车辆到期前30、10、5、3、2、1天，对租赁_销售经理 职责进行提醒
 * 2. 车辆到期前2,1天，对销售领导 进行提醒
 *
 * @author yj
 * @date 2022/10/31 9:29
 */
@Slf4j
@Component
public class ContractExpireRemindScheduler {

    @Resource
    private ServeAggregateRootApi serveAggregateRootApi;
    @Resource
    private CustomerAggregateRootApi customerAggregateRootApi;
    @Resource
    private UserAggregateRootApi userAggregateRootApi;
    @Resource
    private MFWxWorkTools mfWxWorkTools;

    @Scheduled(cron = "0 0 9 * * ?")
    public void process() {

        //获取配置项
        DeliverProjectProperties.ContractExpireNotify contractExpireNotify = DeliverProjectProperties.CONTRACT_EXPIRE_NOTIFY;
        //通知的人的 职责id::提前天数
        List<DeliverProjectProperties.ContractExpireNotifyItem> noticeItems = contractExpireNotify.getItems();
        Map<Integer, List<Integer>> noticeItemMap = noticeItems.stream().collect(Collectors.toMap(
                DeliverProjectProperties.ContractExpireNotifyItem::getDutyId, DeliverProjectProperties.ContractExpireNotifyItem::getAdvanceDays, (key1, key2) -> key1));
        //销售领导 企微id
        String saleLeaderWxId = contractExpireNotify.getSaleLeaderWxId();
        //销售领导停止提前天数
        List<Integer> saleLeaderNotifyAdvanceDays = contractExpireNotify.getSaleLeaderNotifyAdvanceDays();
        if (CollUtil.isEmpty(noticeItems) && StringUtils.isEmpty(saleLeaderWxId)) {
            return;
        }
        //职责id列表,提前天数列表
        Set<Integer> dutyIdSet = noticeItemMap.keySet();
        Set<Integer> dayOffsetSet = new HashSet<>(saleLeaderNotifyAdvanceDays);
        for (DeliverProjectProperties.ContractExpireNotifyItem noticeItem : noticeItems) {
            dayOffsetSet.addAll(noticeItem.getAdvanceDays());
        }
        //转为具体日期
        Date today = new Date();
        Set<String> dates = new HashSet<>(dayOffsetSet.size());
        for (Integer day : dayOffsetSet) {
            Date date = DateUtils.addDays(today, -day);
            dates.add(DateUtil.format(date, DatePattern.NORM_DATE_PATTERN));
        }
        //1,查询维修中,租赁中服务单 30/10/5/3/2/1天后预计收车的 服务单
        //客户id 客户名称 车牌号 合同编号 预计收车日期
        List<Integer> statuses = Arrays.asList(ServeEnum.REPAIR.getCode(), ServeEnum.DELIVER.getCode());
        ContractWillExpireQry contractWillExpireQry = new ContractWillExpireQry();
        contractWillExpireQry.setStatuses(statuses);
        contractWillExpireQry.setExpectRecoverDateList(new ArrayList<>(dates));
        //查询信息
        Result<List<ContractWillExpireInfoDTO>> contractThatWillExpireResult = serveAggregateRootApi.getContractThatWillExpire(contractWillExpireQry);
        List<ContractWillExpireInfoDTO> contractInfoList = ResultDataUtils.getInstance(contractThatWillExpireResult).getDataOrNull();
        if (CollUtil.isEmpty(contractInfoList)) {
            return;
        }
        Map<Integer, List<ContractWillExpireInfoDTO>> orgGroupMsgInfoMap = contractInfoList.stream().collect(Collectors.groupingBy(ContractWillExpireInfoDTO::getOrgId));
        //2,查询客户管理区下 对应职责dutyIds 的雇员
        HashSet<Integer> customerIdSet = new HashSet<>();
        for (ContractWillExpireInfoDTO contractInfo : contractInfoList) {
            customerIdSet.add(contractInfo.getCustomerId());
        }
        Result<List<Customer>> customerListResult = customerAggregateRootApi.getCustomerByIdList(new ArrayList<>(customerIdSet));
        List<Customer> customerList = ResultDataUtils.getInstance(customerListResult).getDataOrNull();
        if (CollUtil.isEmpty(customerList)) {
            return;
        }
        Set<Integer> orgIdSet = new HashSet<>();
        Map<Integer, Customer> customerMap = new HashMap<>();
        for (Customer customer : customerList) {
            customerMap.put(customer.getId(), customer);
            orgIdSet.add(customer.getOrgId());
        }
        //查询雇员  客户所属管理区下,需要通知的职责下的雇员
        EmployeeListByOrgAndDutyQry empQry = EmployeeListByOrgAndDutyQry.builder()
                .orgIdList(new ArrayList<>(orgIdSet))
                .dutyIdList(new ArrayList<>(dutyIdSet))
                .build();
        Result<List<UserDTO>> employeeListResult = userAggregateRootApi.getEmployeeByOrgAndDuty(empQry);
        List<UserDTO> employeeList = ResultDataUtils.getInstance(employeeListResult).getDataOrNull();
        //将雇员按管理区分组
        Map<Integer, List<UserDTO>> orgGroupEmpMap = employeeList.stream().collect(Collectors.groupingBy(UserDTO::getOfficeId));

        //3,组装提醒信息
        //根据org分组处理
        for (Map.Entry<Integer, List<ContractWillExpireInfoDTO>> msgEntry : orgGroupMsgInfoMap.entrySet()) {
            Integer orgId = msgEntry.getKey();
            List<ContractWillExpireInfoDTO> msgInfoList = msgEntry.getValue();
            //要发给的用户
            List<UserDTO> userDTOList = orgGroupEmpMap.get(orgId);
            if (CollUtil.isEmpty(userDTOList)) {
                continue;
            }
            //用户按职责分组,以发送不同的消息
            Map<Integer, List<UserDTO>> dutyGroupUserMap = userDTOList.stream().collect(Collectors.groupingBy(UserDTO::getDutyId));
            //根据职责分组处理
            for (Map.Entry<Integer, List<UserDTO>> dutyUserEntity : dutyGroupUserMap.entrySet()) {
                Integer dutyId = dutyUserEntity.getKey();
                //需要接收消息的雇员
                List<UserDTO> userList = dutyUserEntity.getValue();
                if (CollUtil.isEmpty(userList)) {
                    continue;
                }
                //雇员企微id,存在且不为销售领导的企微id,后续销售领导单独发
                List<String> corpUserIdList = userList.stream()
                        .filter(userDTO -> StringUtils.isNotEmpty(userDTO.getCorpUserId()) && !userDTO.getCorpUserId().equals(saleLeaderWxId))
                        .map(UserDTO::getCorpUserId)
                        .distinct()
                        .collect(Collectors.toList());
                if (CollUtil.isEmpty(corpUserIdList)) {
                    continue;
                }
                //当前职责需要接收的天数
                List<Integer> dayOfficeList = noticeItemMap.get(dutyId);
                Set<String> dutyDates = new HashSet<>(dayOfficeList.size());
                for (Integer day : dayOfficeList) {
                    Date date = DateUtils.addDays(today, -day);
                    dutyDates.add(DateUtil.format(date, DatePattern.NORM_DATE_PATTERN));
                }
                //获取对应时间的 消息类
                msgInfoList = msgInfoList.stream().filter(
                        msgInfo -> dutyDates.contains(msgInfo.getExpectRecoverDate())
                ).collect(Collectors.toList());
                //根据客户分组  组装管理区内信息
                String msg = this.groupByCustomerBuildNotice(msgInfoList, customerMap);
                //4,发送提醒
                this.sendNoticeToWxUser(msg, corpUserIdList);
            }
        }

        //5 总部销售单独发送特殊处理
        if (StringUtils.isNotEmpty(saleLeaderWxId)) {
            //需要接收的天数
            Set<String> dutyDates = new HashSet<>();
            for (Integer day : saleLeaderNotifyAdvanceDays) {
                Date date = DateUtils.addDays(today, -day);
                dutyDates.add(DateUtil.format(date, DatePattern.NORM_DATE_PATTERN));
            }
            //总部销售运营需要发送的对应时间的 消息类
            List<ContractWillExpireInfoDTO> msgInfoList = contractInfoList.stream().filter(
                    msgInfo -> dutyDates.contains(msgInfo.getExpectRecoverDate())
            ).collect(Collectors.toList());
            //构建通知消息
            String msg = this.groupByCustomerBuildNotice(msgInfoList, customerMap);
            //发送
            boolean result = this.sendNoticeToWxUser(msg, Collections.singletonList(saleLeaderWxId));
            log.debug("向销售leader 发送通知 notice:{}   targetUser:{}   result:{}", msg, saleLeaderWxId, result);
        }
    }

    /**
     * 发送企微通知
     */
    private boolean sendNoticeToWxUser(String notice, List<String> wxIdList) {
        if (StringUtils.isEmpty(notice) || CollUtil.isEmpty(wxIdList)) {
            return true;
        }
        WxCpSendMessageDTO wxCpSendMessageDTO = new WxCpSendMessageDTO();
        wxCpSendMessageDTO.setToAll(false);
        //企微 agentId
        DeliverProjectProperties.ContractExpireNotify contractExpireNotify = DeliverProjectProperties.CONTRACT_EXPIRE_NOTIFY;
        Integer wxAgentId = contractExpireNotify.getWxAgentId();
        wxCpSendMessageDTO.setAgentId(wxAgentId);
        wxCpSendMessageDTO.setMsgType(WxMessageTypeEnum.TEXT.getType());
        wxCpSendMessageDTO.setContent(notice);
        wxCpSendMessageDTO.setToUserList(wxIdList);
        Boolean result = false;
        try {
            result = mfWxWorkTools.sendMessage(wxCpSendMessageDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("企微 合同提醒 sendMsg:{}   targetUser:{}   result:{}", notice, wxIdList, result);
        return result;
    }

    /**
     * 根据客户分组拼装通知消息
     */
    private String groupByCustomerBuildNotice(List<ContractWillExpireInfoDTO> msgInfoList, Map<Integer, Customer> customerMap) {
        if (CollUtil.isEmpty(msgInfoList)) {
            return "";
        }
        //根据客户分组  组装org内信息
        Map<Integer, List<ContractWillExpireInfoDTO>> customerContractMap = msgInfoList.stream().collect(Collectors.groupingBy(ContractWillExpireInfoDTO::getCustomerId));
        //转为消息类
        List<NoticeTemplateInfoDTO> loopNoticeTemplateInfoDTOList = new ArrayList<>();
        customerContractMap.forEach((customerId, customerContractInfoList) -> {
            NoticeTemplateInfoDTO noticeTemplateInfoDTO = new NoticeTemplateInfoDTO();
            if (CollUtil.isNotEmpty(customerMap) && customerMap.get(customerId) != null) {
                Customer customer = customerMap.get(customerId);
                noticeTemplateInfoDTO.setCustomerName(customer.getName());
                List<String> licensePlateList = customerContractInfoList.stream().map(ContractWillExpireInfoDTO::getCarNum).collect(Collectors.toList());
                noticeTemplateInfoDTO.setLicensePlateList(licensePlateList);
                noticeTemplateInfoDTO.setCarNumber(licensePlateList.size());
                loopNoticeTemplateInfoDTOList.add(noticeTemplateInfoDTO);
            }
        });
        //拼装
        ContractExpireNotifyDTO contractExpireNotifyDTO = ContractExpireNotifyDTO.builder().loopTemplate(loopNoticeTemplateInfoDTOList).build();
        //格式化模板
        return this.formatTemplate(contractExpireNotifyDTO);
    }

    /**
     * 格式化总模板
     */
    public String formatTemplate(ContractExpireNotifyDTO contractExpireNotifyDTO) {
        //合同过期提醒配置
        DeliverProjectProperties.ContractExpireNotify contractExpireNotify = DeliverProjectProperties.CONTRACT_EXPIRE_NOTIFY;
        //模板格式化规则
        List<DeliverProjectProperties.FormatRule> formatRules = contractExpireNotify.getFormatRules();
        //格式化规则转map
        Map<String, DeliverProjectProperties.FormatRule> formatRuleMap =
                formatRules.stream().collect(Collectors.toMap(DeliverProjectProperties.FormatRule::getSourceFieldName, Function.identity(), (k1, k2) -> k1));
        //获取相关配置
        DeliverProjectProperties.NotifyTemplate commonNoticeTemplate = contractExpireNotify.getCommonNoticeTemplate();
        //通知模板
        String noticeTemplate = commonNoticeTemplate.getNoticeTemplate();
        //是否存在循环模板
        Boolean hasLoopTemplate = commonNoticeTemplate.getHasLoopTemplate();
        //循环模板
        String loopTemplate = commonNoticeTemplate.getLoopTemplate();
        //循环模板分隔符
        String loopTemplateSeparator = commonNoticeTemplate.getLoopTemplateSeparator();
        //循环模板替换的目标
        String loopTemplateFormat = commonNoticeTemplate.getLoopTemplateFormat();
        //属性 按替换顺序 降序排列
        CollUtil.sort(formatRules, (o1, o2) -> o2.getReplaceSort() - o1.getReplaceSort());
        //模板建立
        for (DeliverProjectProperties.FormatRule field : formatRules) {
            if (!field.getAutoReplace()) {
                continue;
            }
            String targetString = field.getTargetString();
            String format = field.getFormat();
            loopTemplate = loopTemplate.replaceAll(targetString, format);
        }
        //获取反射类
        Class<? extends ContractExpireNotifyDTO> notifyDtoClass = contractExpireNotifyDTO.getClass();
        //获取循环模板数据
        List<NoticeTemplateInfoDTO> loopNoticeTemplateInfoDTOList = contractExpireNotifyDTO.getLoopTemplate();

        String notice = "";
        //替换循环模板
        if (hasLoopTemplate && CollUtil.isNotEmpty(loopNoticeTemplateInfoDTOList)) {
            Class<? extends NoticeTemplateInfoDTO> noticeClass = loopNoticeTemplateInfoDTOList.get(0).getClass();
            Field[] classFields = noticeClass.getDeclaredFields();
            //替换属性
            List<String> loopTemplateList = new ArrayList<>();
            for (NoticeTemplateInfoDTO noticeTemplateInfoDTO : loopNoticeTemplateInfoDTOList) {
                String loopMsg = loopTemplate;
                JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(noticeTemplateInfoDTO));
                //比对属性,并替换
                loopMsg = this.formatAndReplaceTemplate(classFields, jsonObject, loopMsg, formatRuleMap);
                loopTemplateList.add(loopMsg);

            }
            String loopTemplateResult = String.join(loopTemplateSeparator, loopTemplateList).concat(loopTemplateSeparator);

            notice = noticeTemplate.replaceAll(loopTemplateFormat, loopTemplateResult);
        }

        //其他属性
        Field[] notifyFields = notifyDtoClass.getDeclaredFields();
        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(contractExpireNotifyDTO));
        notice = this.formatAndReplaceTemplate(notifyFields, jsonObject, notice, formatRuleMap);

        return notice;
    }

    /**
     * 格式化循环模板
     * 将classJsonObj 内对应的 消息类的JSON 对象 替换 模板消息 内的表达式
     *
     * @param classFields   消息类的属性集
     * @param classJsonObj  消息类的JSON 对象
     * @param templateMsg   模板消息
     * @param formatRuleMap 模板格式化规则map  key:fieldName value:FormatRule
     */
    public String formatAndReplaceTemplate(Field[] classFields, JSONObject classJsonObj, String templateMsg, Map<String, DeliverProjectProperties.FormatRule> formatRuleMap) {


        for (Field classField : classFields) {
            String fieldName = classField.getName();
            DeliverProjectProperties.FormatRule formatRule = formatRuleMap.get(fieldName);

            //属性存在于模板,且属性不为null,替换
            if (formatRule != null && formatRule.getAutoReplace() && templateMsg.contains(fieldName) && classJsonObj.get(fieldName) != null) {
                boolean list = formatRule.getIsList();
                String format = formatRule.getFormat();
                String sourceFieldName = formatRule.getSourceFieldName();
                String separator = formatRule.getSeparator();
                //属性值
                Object fieldValue = classJsonObj.get(fieldName);

                String fieldFormatResult;
                //list类型属性
                if (list && fieldValue instanceof List) {
                    String itemFormat = formatRule.getItemFormat();
                    String itemName = formatRule.getItemName();
                    @SuppressWarnings("unchecked")
                    List<Object> valueList = (List<Object>) fieldValue;
                    if (CollUtil.isEmpty(valueList)) {
                        continue;
                    }
                    List<String> result = new ArrayList<>();
                    for (Object value : valueList) {
                        //车牌号为null
                        if (value == null) {
                            continue;
                        }
                        String itemStr = itemFormat.replaceAll(itemName, value.toString());
                        result.add(itemStr);
                    }
                    fieldFormatResult = String.join(separator, result);
                }
                //其他类型
                else {
                    fieldFormatResult = format.replaceAll(sourceFieldName, fieldValue.toString());
                }
                templateMsg = templateMsg.replaceAll(format, fieldFormatResult);
            }
        }
        return templateMsg;
    }


}
