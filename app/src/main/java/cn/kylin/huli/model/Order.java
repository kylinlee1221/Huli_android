package cn.kylin.huli.model;

public class Order {
    private String ordername;
    private Double orderprice;
    private String orderplace;
    private Double orderpaid;
    private String orderDate;
    private Long id;
    public Order(Long id,String ordername,String orderplace,String orderDate,Double orderprice,Double orderpaid){
        this.id=id;
        this.ordername=ordername;
        this.orderplace=orderplace;
        this.orderDate=orderDate;
        this.orderprice=orderprice;
        this.orderpaid=orderpaid;
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

}
