package com.parse.starter;

public class Booking {

    //region variabile
    private String departure;
    private String arrival;
    private String departureDate;
    private String departureHour;
    private String arrivalHour;
    private double price;
    private String company;
    private int companyImageID;
    private String series;
    private String issueDate;
    private String currency="lei";
    //endregion

    public Booking (String departure, String arrival, String departureDate, String departureHour, String arrivalHour, double price, String company, int companyImageID, String series, String issueDate) {
        this.departure = departure;
        this.arrival = arrival;
        this.departureDate = departureDate;
        this.departureHour = departureHour;
        this.arrivalHour = arrivalHour;
        this.price = price;
        this.company = company;
        this.companyImageID = companyImageID;
        this.series = series;
        this.issueDate = issueDate;
    }

    //region getteri si setteri

    public String getDeparture () {
        return departure;
    }
    public void setDeparture (String departure) {
        this.departure = departure;
    }

    public String getArrival() {
        return arrival;
    }
    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getDepartureDate() {
        return departureDate;
    }
    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getDepartureHour() {
        return departureHour;
    }
    public void setDepartureHour(String departureHour) {
        this.departureHour = departureHour;
    }

    public String getArrivalHour() {
        return arrivalHour;
    }
    public void setArrivalHour(String arrivalHour) {
        this.arrivalHour = arrivalHour;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    public int getCompanyImageID() {
        return companyImageID;
    }
    public void setCompanyImageID(int companyImageID) {
        this.companyImageID = companyImageID;
    }

    public String getSeries() {
        return series;
    }
    public void setSeries (String series) {
        this.series = series;
    }

    public String getIssueDate() {
        return issueDate;
    }
    public void setIssueDate (String issueDate) {
        this.issueDate = issueDate;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    //endregion

}
