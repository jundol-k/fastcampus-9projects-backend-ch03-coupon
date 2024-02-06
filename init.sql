create table coupons
(
    title                varchar(255) not null comment '쿠폰명',
    coupon_type          varchar(255) not null comment '쿠폰타입 (선착순 쿠폰...)',
    total_quantity       int          null comment '쿠폰 발급 최대 수량',
    issued_quantity      int          not null comment '발급된 쿠폰 수량',
    discount_amount      int          not null comment '할인 금액',
    min_available_amount int          not null comment '최소 사용 금액',
    date_issue_start     datetime     not null comment '발급 시작 일시',
    date_issue_end       datetime     not null comment '발급 종료 일시',
    date_created         datetime     not null comment '생성 일시',
    date_updated         datetime     not null comment '수정 일시',
    id                   bigint       not null
        primary key
);

create table coupon_issues
(
    id           bigint   not null
        primary key,
    user_id      bigint   not null comment '유저 ID',
    date_issued  datetime not null comment '발급 일시',
    date_used    datetime null comment '사용 일시',
    date_created datetime not null comment '생성 일시',
    date_updated datetime not null comment '수정 일시',
    coupon_id    bigint   not null,
    constraint coupon_issues_coupons_id_fk
        foreign key (coupon_id) references coupons (id)
);

