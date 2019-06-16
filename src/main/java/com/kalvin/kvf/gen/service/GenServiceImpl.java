package com.kalvin.kvf.gen.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.kalvin.kvf.gen.utils.AuxiliaryKit;
import com.kalvin.kvf.gen.comm.ConfigConstant;
import com.kalvin.kvf.gen.dto.ButtonConfigDTO;
import com.kalvin.kvf.gen.dto.TableColumnDTO;
import com.kalvin.kvf.gen.vo.GenConfigVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Create by Kalvin on 2019/6/15
 */
@Service
public class GenServiceImpl implements IGenService {

    private final static Logger LOGGER = LoggerFactory.getLogger(GenServiceImpl.class);

    @Autowired
    private ITableService tableService;

    @Override
    public GenConfigVO init(String tableName, String tableType) {
        GenConfigVO genConfig = new GenConfigVO(tableName, tableType);
        String moduleName = tableName.substring(0, tableName.indexOf("_"));
        String funName = StrUtil.toCamelCase(tableName.substring(tableName.indexOf("_") + 1));
        genConfig.setModuleName(moduleName).setFunName(funName);

        List<TableColumnDTO> tableColumnDTOS = tableService.listTableColumn(tableName);
        tableColumnDTOS = AuxiliaryKit.handleTableColumns(tableColumnDTOS);

        Optional<TableColumnDTO> tOptional = tableColumnDTOS
                .stream().filter(tc -> tc.getColumnKey().equals("PRI")).findFirst();
        TableColumnDTO tableColumn = tOptional.get();
        genConfig.setPrimaryKey(tableColumn.getColumnName());

        genConfig.setColumns(AuxiliaryKit.tableColumnsToColumnConfigs(tableColumnDTOS));

        String buttonInfo = FileUtil.readString(
                new File(ClassUtil.getClassPath() + ConfigConstant.BUTTON_JSON_REL_PATH), "UTF-8");
        JSONObject jsonObject = JSONUtil.parseObj(buttonInfo);
        JSONArray headButtons = JSONUtil.parseArray(jsonObject.get(ConfigConstant.HEAD_BUTTON_KEY));
        JSONArray rowButtons = JSONUtil.parseArray(jsonObject.get(ConfigConstant.ROW_BUTTON_KEY));
        genConfig.setHeadButtons(JSONUtil.toList(headButtons, ButtonConfigDTO.class));
        genConfig.setRowButtons(JSONUtil.toList(rowButtons, ButtonConfigDTO.class));
        genConfig.setQueryColumns(new ArrayList<>());

        return genConfig;
    }
}