package com.parse.starter;

import java.util.Comparator;

public class Result {

    //region variabile
    private String departure;
    private String arrival;
    private String departureDate;
    private String departureHour;
    private String arrivalHour;
    private double price;
    private String currency="lei";
    private String company;
    private int companyImageID;
    private int idRide;
    //endregion

    //Constructor
    public Result(String departure, String arrival, String departureDate, String departureHour, String arrivalHour, double price, String company, int companyImageID , int idRide) {
        this.departure = departure;
        this.arrival = arrival;
        this.departureDate = departureDate;
        this.departureHour = departureHour;
        this.arrivalHour = arrivalHour;
        this.price = price;
        this.company = company;
        this.companyImageID = companyImageID;
        this.idRide = idRide;
    }


    //region Comparator for sorting the list by Price and Hour

    public static Comparator<Result> priceAscendingComparator = new Comparator<Result>() {
        public int compare(Result r1, Result r2) {
            double result1 = r1.getPrice();
            double result2 = r2.getPrice();
            if (result1 > result2) return 1;
            else return -1;
        }
    };

    public static Comparator<Result> priceDescendingComparator = new Comparator<Result>() {
        public int compare(Result r1, Result r2) {
            double result1 = r1.getPrice();
            double result2 = r2.getPrice();
            if (result1 < result2) return 1;
            else return -1;
        }
    };

    public static Comparator<Result> hourAscendingComparator = new Comparator<Result>() {
        public int compare(Result r1, Result r2) {
            String result1 = r1.getDepartureHour().toUpperCase();
            String result2 = r2.getDepartureHour().toUpperCase();
            return result1.compareTo(result2);
        }
    };

    public static Comparator<Result> hourDescendingComparator = new Comparator<Result>() {
        public int compare(Result r1, Result r2) {
            String result1 = r1.getDepartureHour().toUpperCase();
            String result2 = r2.getDepartureHour().toUpperCase();
            return result2.compareTo(result1);
        }
    };

    //endregion

    //region getters and setters

    public String getDeparture() {
        return departure;
    }
    public void setDeparture(String departure) {
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

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getCompanyImageID() {
        return companyImageID;
    }
    public void setCompanyImageID(int companyImageID) {
        this.companyImageID = companyImageID;
    }

    public int getIdRide() {
        return idRide;
    }
    public void setIdRide(int idRide) {
        this.idRide = idRide;
    }

    //endregion
}
