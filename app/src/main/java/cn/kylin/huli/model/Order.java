package cn.kylin.huli.model;

public class Order {
    private String ordername;
    private Double orderprice;
    private String orderplace;
    private Double orderpaid;
    private String orderDate;
    private String orderStart;
    private String orderStatus;
    private String orderPhone;
    private String orderFrom;
    private Long id;

    public Order(Long id,String ordername,String orderplace,String orderDate,String orderStart,Double orderprice,Double orderpaid,String orderStatus,String orderPhone){
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderDate;
        this.orderprice=orderprice;
        this.orderStart=orderStart;
        this.orderpaid=orderpaid;
        this.orderStatus=orderStatus;
        this.orderPhone=orderPhone;
    }

    public Order(Long id,String ordername,String orderplace,String orderDate,String orderStart,Double orderprice,Double orderpaid,String orderStatus,String orderPhone,String orderFrom){
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderDate;
        this.orderprice=orderprice;
        this.orderStart=orderStart;
        this.orderpaid=orderpaid;
        this.orderStatus=orderStatus;
        this.orderPhone=orderPhone;
        this.orderFrom=orderFrom;
    }

    public Order(Long id,String ordername,String orderplace,String orderDate,Double orderprice,Double orderpaid,String orderStatus,String orderPhone){
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderDate;
        this.orderprice=orderprice;
        this.orderpaid=orderpaid;
        this.orderStatus=orderStatus;
        this.orderPhone=orderPhone;
    }

    public Order(Long id,String ordername,String orderplace,String orderDate,Double orderprice,Double orderpaid,String orderStatus){
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderDate;
        this.orderprice=orderprice;
        this.orderpaid=orderpaid;
        this.orderStatus=orderStatus;
    }

    public Order(Long id,String ordername,String orderplace,String orderDate,Double orderprice,Double orderpaid){
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderDate;
        this.orderprice=orderprice;
        this.orderpaid=orderpaid;
    }

    public Order(Long id, String ordername, String orderplace, String orderend, String orderStart, Double orderprice, Double orderpaid, String orderStatus) {
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderend;
        this.orderStart=orderStart;
        this.orderprice=orderprice;
        this.orderpaid=orderpaid;
        this.orderStatus=orderStatus;
    }

    public String getOrderFrom() {
        return orderFrom;
    }

    public void setOrderFrom(String orderFrom) {
        this.orderFrom = orderFrom;
    }

    public String getOrderStart() {
        return orderStart;
    }

    public void setOrderStart(String orderStart) {
        this.orderStart = orderStart;
    }

    public String getOrderPhone() {
        return orderPhone;
    }

    public void setOrderPhone(String orderPhone) {
        this.orderPhone = orderPhone;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getOrderpaid() {
        return orderpaid;
    }

    public Double getOrderprice() {
        return orderprice;
    }

    public Long getId() {
        return id;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getOrdername() {
        return ordername;
    }

    public String getOrderplace() {
        return orderplace;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public void setOrdername(String ordername) {
        this.ordername = ordername;
    }

    public void setOrderpaid(Double orderpaid) {
        this.orderpaid = orderpaid;
    }

    public void setOrderplace(String orderplace) {
        this.orderplace = orderplace;
    }

    public void setOrderprice(Double orderprice) {
        this.orderprice = orderprice;
    }

    @Override
    public String toString() {
        return "Order{" +
                "ordername='" + ordername + '\'' +
                ", orderprice=" + orderprice +
                ", orderplace='" + orderplace + '\'' +
                ", orderpaid=" + orderpaid +
                ", orderDate='" + orderDate + '\'' +
                ", id=" + id +
                '}';
    }
}
