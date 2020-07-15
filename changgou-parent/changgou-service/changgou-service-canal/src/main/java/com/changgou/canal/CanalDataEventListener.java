package com.changgou.canal;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.*;

/**
 * @author ：zxq
 * @date ：Created in 2020/7/15 16:28
 */
@CanalEventListener
public class CanalDataEventListener {


    /***
     * 增加数据监听
     * rowData.getAfterColumnsList() 增加，修改
     *
     * @param eventType 当前操作的类型 ： 增加数据
     * @param rowData 发生变更的一行数据
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    /***
     * 修改数据监听
     * @param rowData
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.RowData rowData) {
        System.out.println("UpdateListenPoint");
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("修改前：" + column.getName() + " --- " + column.getValue());
        }

        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    /***
     * 删除数据监听
     * @param eventType
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.out.println("DeleteListenPoint");

        rowData.getBeforeColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));

    }

    /***
     * 自定义数据修改监听
     * @param eventType
     * @param rowData
     */
    @ListenPoint(
            destination = "example",
            schema = "changgou_content",
            table = {"tb_content_category", "tb_content"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE})
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.err.println("DeleteListenPoint");
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            System.out.println("自定义前：" + column.getName() + " --- " + column.getValue());
        }

        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

}
