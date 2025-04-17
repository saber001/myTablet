package com.example.mytablet.ui.model;

public class BoardInfo {

    public RoomBean room;
    public SiteInfoBean siteInfo;

    public String now;
    public class RoomBean {
        /**
         * createBy : admin
         * createTime : 2025-03-05 20:37:38
         * updateBy : admin
         * updateTime : 2025-03-05 20:37:38
         * id : 1897265241058713608
         * roomName : 手工园艺教室2
         * capacity : 25
         * area : 80
         * address : 社区活动楼1-2
         * bpSn : bfcc2b9ab3bc770a
         * cameraSn : null
         * status : enable
         */

        public String createBy;
        public String createTime;
        public String updateBy;
        public String updateTime;
        public String id;
        public String roomName;
        public int capacity;
        public int area;
        public String address;
        public String bpSn;
        public Object cameraSn;
        public String status;
    }

    public class SiteInfoBean {
        /**
         * unit : 龙泉社区活动中心
         * phone : 13800138000
         * about : 走进锦林社区活动室，一群乒乓球爱好者正在练习乒乓球。居民王大姐说：“冬季外面冰雪覆盖，社区活动室内却温暖如春，我们一群乒乓球爱好者在这里愉快地打上几局，活动活动筋骨。”棋牌室里，居民们三五成群，热闹非凡。社区的老年人歌唱团成员，更是个个精神抖擞，每天都聚在一起唱歌跳舞。这些娱乐项目不仅丰富了居民们的文化生活，还增进了邻里之间的友谊。
         * logo : 1905284921154134018
         * logoUrl : http://ccms.sczhiming.cn/prod-api/_oss_/2025/03/27/67bf7f80c4744ad4ad41978a7507aef1.png
         * qrCode : 1905284297435963394
         * qrCodeUrl : http://ccms.sczhiming.cn/prod-api/_oss_/2025/03/27/30d0d1f7b0f8469c93dd6dca61972149.jpeg
         * userGuide : 1905284986849517569
         * userGuideUrl : http://ccms.sczhiming.cn/prod-api/_oss_/2025/03/27/0eaab955adef40c78753493ce65bdd6a.jpeg
         * updateTime : 2025-03-28 00:06:11
         */

        public String unit;
        public String phone;
        public String about;
        public String logo;
        public String logoUrl;
        public String qrCode;
        public String qrCodeUrl;
        public String userGuide;
        public String userGuideUrl;
        public String updateTime;
    }
}
