package com.mfexpress.rent.deliver.recovervehicle;

public enum RecoverEnum {
    APPLY_LIST_ALL(1, "recoverApplyListAll"),
    APPLY_LIST_STAY(2, "recoverApplyListStay"),
    APPLY_LIST_COMPLETED(3, "recoverApplyListCompleted"),
    TASK_LIST_ALL(4, "recoverTaskListAll"),
    TASK_LIST_CHECK(5, "recoverTaskListCheck"),
    TASK_LIST_INSURE(6, "recoverTaskListInsure"),
    TASK_LIST_DEDUCTION(7, "recoverTaskListDeduction"),
    TASK_LIST_COMPLETED(8, "recoverTaskListCompleted");

    private Integer tag;

    private String serviceName;

    RecoverEnum(Integer tag, String serviceName) {
        this.tag = tag;
        this.serviceName = serviceName;
    }

    public static String getServiceName(Integer tag) {
        for (RecoverEnum recoverEnum : RecoverEnum.values()) {
            if (recoverEnum.tag.equals(tag)) {
                return recoverEnum.serviceName;
            }
        }
        return "";
    }


}
