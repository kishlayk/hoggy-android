package com.my.kiki.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class TicketsModel {

//    String ticketId;
    String ticketTitle;
    String ticketDesc;
    String userEmail;
    String ticketStatus;
    @ServerTimestamp
    Date ticketPostedDate;


    /*public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }*/

    public String getTicketTitle() {
        return ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getTicketDesc() {
        return ticketDesc;
    }

    public void setTicketDesc(String ticketDesc) {
        this.ticketDesc = ticketDesc;
    }

    public Date getTicketPostedDate() {
        return ticketPostedDate;
    }

    public void setTicketPostedDate(Date ticketPostedDate) {
        this.ticketPostedDate = ticketPostedDate;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}
