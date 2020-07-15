//package com.changgou.canal;
//
//import com.alibaba.otter.canal.protocol.CanalEntry;
//import com.xpand.starter.canal.annotation.*;
//
///**
// * @author ：zxq
// * @date ：Created in 2020/7/15 16:28
// */
//@CanalEventListener
//public class CanalDataEventListenerTest {
//
//
//    /***
//     * 增加数据监听
//     * rowData.getAfterColumnsList() 增加，修改
//     * rowData.getBeforeColumnsList()  修改，删除
//     * @param eventType 当前操作的类型 ： 增加数据
//     * @param rowData 发生变更的一行数据
//     */
//    @InsertListenPoint
//    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        System.out.println("---onEventInsert---");
//        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
//            System.out.println("添加后: " + column.getName() + ":" + column.getValue());
//        }
//    }
//
//    /***
//     * 修改数据监听
//     * @param rowData
//     */
//    @UpdateListenPoint
//    public void onEventUpdate(CanalEntry.RowData rowData) {
//        System.out.println("---onEventUpdate---");
//
//        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
//            System.out.println("修改前: " + column.getName() + ":" + column.getValue());
//        }
//
//        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
//            System.out.println("修改后: " + column.getName() + ":" + column.getValue());
//        }
//    }
//
//    /***
//     * 删除数据监听
//     * @param eventType
//     */
//    @DeleteListenPoint
//    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        System.out.println("---onEventDelete---");
//
//        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
//            System.out.println("删除前: " + column.getName() + ":" + column.getValue());
//        }
//    }
//
//    /***
//     * 自定义数据修改监听
//     * @param eventType
//     * @param rowData
//     */
//    @ListenPoint(
//            destination = "example",
//            schema = "changgou_content",
//            table = {"tb_content_category", "tb_content"},
//            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.DELETE})
//    public void onEventCustom(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
////        System.out.println("---onEventCustom---");
////
////        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
////            System.out.println("onEventCustom 前: " + column.getName() + ":" + column.getValue());
////        }
////
////        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
////            System.out.println("onEventCustom 后: " + column.getName() + ":" + column.getValue());
////        }
//    }
//
//}
