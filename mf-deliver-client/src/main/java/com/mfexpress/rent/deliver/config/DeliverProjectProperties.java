package com.mfexpress.rent.deliver.config;

import com.mfexpress.rent.deliver.constant.Constants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yj
 * @date 2022/10/24 8:57
 */
@Data
@Component
@ConfigurationProperties(prefix = Constants.PROPERTIES_PREFIX)
@EnableConfigurationProperties
public class DeliverProjectProperties implements InitializingBean {

    private TimeRange recoverTimeRange;
    private TimeRange deliverTimeRange;
    private ContractExpireNotify contractExpireNotify;


    public static TimeRange RECOVER_TIME_RANGE;
    public static TimeRange DELIVER_TIME_RANGE;
    public static ContractExpireNotify CONTRACT_EXPIRE_NOTIFY;

    @Override
    public void afterPropertiesSet() throws Exception {
        RECOVER_TIME_RANGE = recoverTimeRange;
        DELIVER_TIME_RANGE = deliverTimeRange;
        CONTRACT_EXPIRE_NOTIFY = contractExpireNotify;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeRange {

        /**
         * T-
         */
        private Integer pre;
        /**
         * T+
         */
        private Integer suf;

    }

    /*
     *      properties:
     *         recoverTimeRange:
     *           pre: 6
     *           suf: 6
     *         deliverTimeRange:
     *           pre: 6
     *           suf: 6
     *         #合同通知配置
     *         contractExpireNotify:
     *           #企微agentId
     *           wxAgentId: 1000005
     *           #公共通知模板
     *           commonNoticeTemplate:
     *             #通知模板
     *             noticeTemplate: "#LOOP#以上车辆合同即将到期，请与客户沟通进行合同续签并及时在系统中操作合同续签。"
     *             #时候包含循环模板
     *             hasLoopTemplate: true
     *             #循环模板格式化部分
     *             loopTemplateFormat:  "#LOOP#"
     *             #循环模板
     *             loopTemplate: "#客户名称#租赁的车辆#车牌号列表#共#车数量#台"
     *             #循环模板分隔符
     *             loopTemplateSeparator: ";\n"
     *           #格式化规则  将模板中的所有targetString 替换为 format,再讲format中的属性名称sourceFieldName,替换为类内属性
     *           formatRules:
     *               #属性名称
     *             - sourceFieldName: loopTemplate
     *               #替换目标
     *               targetString: "#LOOP#"
     *               #将sourceFieldName格式化为format
     *               format: loopTemplate
     *               #是否为列表
     *               isList: true
     *               #列表项格式化为
     *               itemFormat: "#客户名称#租赁的车辆#车牌号列表#共#车数量#台"
     *               #列表项名称
     *               itemName: "loopItem"
     *               #列表项分隔符
     *               separator: ";\n"
     *               #替换顺序,降序,循环模板优先级设为最高
     *               replaceSort: 2147483647
     *             - sourceFieldName: customerName
     *               targetString: "#客户名称#"
     *               format: "\"customerName\""
     *               isList: false
     *             - sourceFieldName: licensePlateList
     *               targetString: "#车牌号列表#"
     *               format: "\"licensePlateList\""
     *               isList: true
     *               itemFormat: "\"item\""
     *               itemName: "item"
     *               separator: "、"
     *             - sourceFieldName: carNumber
     *               targetString: "#车数量#"
     *               format: carNumber
     *           #销售领导企微id
     *           saleLeaderWxId: 13001801335
     *           #销售领导提醒提前的天数 T-N
     *           saleLeaderNotifyAdvanceDays:
     *             - 2
     *             - 1
     *           items:
     *               #职责
     *             - dutyId: 6
     *               #职责提醒提前的天数  T-N
     *               advanceDays:
     *                 - 30
     *                 - 10
     *                 - 5
     *                 - 3
     *                 - 2
     *                 - 1
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContractExpireNotify {

        /**
         * 企微代理认证id agentId
         */
        private Integer wxAgentId;
        /**
         * 需要格式化的公共通知模板
         */
        private NotifyTemplate commonNoticeTemplate;
        /**
         * 格式化规则
         */
        private List<FormatRule> formatRules;
        /**
         * 合同通知那些人配置
         */
        private List<ContractExpireNotifyItem> items;
        /**
         * 总部销售运营 企微id
         */
        private String saleLeaderWxId;
        /**
         * 总部销售运营 企微id  通知提前天数
         */
        private List<Integer> saleLeaderNotifyAdvanceDays;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ContractExpireNotifyItem {

        /**
         * 职责id
         */
        private Integer dutyId;
        /**
         * 提前天数
         */
        private List<Integer> advanceDays;
//        /**
//         * 通知模板  format {} 优先
//         */
//        private NotifyTemplate noticeTemplate;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotifyTemplate {

        /**
         * 通知模板
         * 例:
         * #LOOP#\n
         * 以上车辆合同即将到期，请与客户沟通进行合同续签并及时在系统中操作合同续签。
         */
        private String noticeTemplate;

        /**
         * 是否包含循环模板部分
         */
        private Boolean hasLoopTemplate = false;

        /**
         * 循环模板
         * 例:
         * #客户名称#租赁的车辆#车牌号列表#共#车数量#台
         *
         * <p>
         * 以上车辆合同即将到期，请与客户沟通进行合同续签并及时在系统中操作合同续签。
         */
        private String loopTemplate;
        /**
         * 循环模板在通知模板中的替换目标字符串
         * 如 "LOOP"
         */
        private String loopTemplateFormat;

        /**
         * 循环模板分隔符
         * 例 ;
         */
        private String loopTemplateSeparator;


    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FormatRule {

        public String sourceFieldName = "";
        /**
         * 替换的模板字符串
         */
        public String targetString = "";
        /**
         * 替换为的字符串
         */
        public String format = "";
        /**
         * 是否为列表类型
         */
        public Boolean isList = false;
        /**
         * 列表类型属性  元素替换的目标字符串
         */
        public String itemFormat = "";
        /**
         * 元素名
         */
        public String itemName = "";
        /**
         * 元素分隔符
         */
        public String separator = "";
        /**
         * 是否需要替换
         */
        public Boolean autoReplace = true;
        /**
         * 替换顺序  数字越小,优先级越大
         */
        public Integer replaceSort = 1;


    }
}
