package com.mfexpress.rent.deliver.utils;

import com.mfexpress.common.domain.api.DictAggregateRootApi;
import com.mfexpress.common.domain.dto.DictDataDTO;
import com.mfexpress.common.domain.dto.DictTypeDTO;
import com.mfexpress.component.response.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommonUtil {

    // 获取字典数据，转为map
    public static Map<String, String> getDictDataDTOMapByDictType(DictAggregateRootApi api, String dictType) {
        if(StringUtils.isEmpty(dictType)){
            return null;
        }
        DictTypeDTO dictTypeDTO = new DictTypeDTO();
        dictTypeDTO.setDictType(dictType);

        Result<Map<String, List<DictDataDTO>>> dictDataResult = api.getAllTypeAsDictionary(Collections.singletonList(dictType));
        if (null == dictDataResult || null == dictDataResult.getData() || null == dictDataResult.getData().get(dictType) || dictDataResult.getData().get(dictType).isEmpty()){
            return null;
        }
        List<DictDataDTO> dictDataDTOS = dictDataResult.getData().get(dictType);
        return dictDataDTOS.stream().collect(Collectors.toMap(DictDataDTO::getDictValue, DictDataDTO::getDictLabel, (key1, key2) -> key1));
    }

}
