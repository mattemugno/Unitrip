package dto;

import com.ericsson.otp.erlang.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Trip {
    String tripName;
    String destination;
    LocalDate date;
    String founder;
    int seats;
    ArrayList<String> participants;
    ArrayList<String> favorites;


    public Trip(String tripName, String destination, LocalDate date, String founder, int seats) {
        this.tripName = tripName;
        this.destination = destination;
        this.founder = founder;
        this.date = date;
        this.seats = seats;
    }

    public Trip(String tripName, String destination, LocalDate date, String founder, int seats, ArrayList<String> participants, ArrayList<String> favorites) {
        this.tripName = tripName;
        this.destination = destination;
        this.date = date;
        this.founder = founder;
        this.seats = seats;
        this.participants = participants;
        this.favorites = favorites;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getFounder() {
        return founder;
    }

    public void setFounder(String founder) {
        this.founder = founder;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }

    public ArrayList<String> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<String> particpants) {
        this.participants = particpants;
    }

    public ArrayList<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(ArrayList<String> favorites) {
        this.favorites = favorites;
    }

    public long ExpirationDate(LocalDate date){
        return java.sql.Date.valueOf(date).getTime() - 1000*7*24*60*60;
    }

    public static Trip parseErlang(OtpErlangList record) throws OtpErlangRangeException, ParseException {
        System.out.println("TRIP TO BE PRINTED ON THE PAGE --> " + record);
        ArrayList<String> participants = new ArrayList<>();
        ArrayList<String> favorites = new ArrayList<>();
        String destination = record.elementAt(3).toString().replace('"',' ').trim();
        //System.out.println("DEST: " + destination);
        long dateErlang = Long.parseLong(String.valueOf(record.elementAt(4)));
        //System.out.println("DAta in milli: " + dateErlang);
        LocalDate date = Instant.ofEpochMilli(dateErlang).atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        //LocalDate date = LocalDate.parse(dateErlang, format);
        //System.out.println("DATE: " + date);

        String founder = record.elementAt(2).toString().replace('"',' ').trim();
        //System.out.println("FOUNDER: " + founder);
        String tripName = record.elementAt(0).toString().replace('"',' ').trim();;
        //System.out.println("NAME: " + tripName);
        int seats = Integer.parseInt(String.valueOf(record.elementAt(5)));
        //System.out.println("SEATS: " + seats);
        OtpErlangList parts = (OtpErlangList) record.elementAt(6);
        OtpErlangList favs = (OtpErlangList) record.elementAt(7);
        //System.out.println("LIST: " + favs);
        for (OtpErlangObject person : parts) {
            //System.out.println(person.toString());
            participants.add(person.toString().replace('"',' ').trim());
        }

        for (OtpErlangObject person : favs) {
            //System.out.println(person.toString());
            favorites.add(person.toString().replace('"',' ').trim());
        }
        //System.out.println("LEN: " + participants.size());
        //System.out.println("ARR: " + participants);

        return  new Trip(tripName, destination, date, founder, seats, participants, favorites);
    }


}
