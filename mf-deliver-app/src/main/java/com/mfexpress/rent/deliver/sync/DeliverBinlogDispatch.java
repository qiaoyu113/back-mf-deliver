package com.mfexpress.rent.deliver.sync;

import com.mfexpress.component.dto.cdc.binlog.dispatch.BinlogDispatch;
import com.mfexpress.rent.deliver.api.SyncServiceI;
import com.mfexpress.rent.deliver.constant.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class DeliverBinlogDispatch extends BinlogDispatch {

    private SyncServiceI serviceI;

    public void setServiceI(SyncServiceI syncServiceI) {
        this.serviceI = syncServiceI;
    }

    @Override
    public void invoker(String database, String table, String type, List<Map<String, String>> data) {
        log.info("database:" + database + " table:" + table + " type:" + type);

        if (type.equals("QUERY") || type.equals("DELETE")) {
            return;
        }

        if (data == null || data.isEmpty()) {
            return;
        }
        log.info("data:" + data);

        if (database.equals(Constants.BINLOG_MQ_DELIVER_DATA_BASE_NAME)) {
            if (table.equals(Constants.BINLOG_MQ_DELIVER_TABLE) || table.equals(Constants.BINLOG_MQ_SERVE_TABLE) ||
                    table.equals(Constants.BINLOG_MQ_DELIVER_VEHICLE_TABLE) || table.equals(Constants.BINLOG_MQ_RECOVER_VEHICLE_TABLE)) {
                for (Map<String, String> item : data) {
                    serviceI.execOne(item.get("serve_no"), table, type);
                }
            }
        }

        log.info("invoker finish");
    }
}
