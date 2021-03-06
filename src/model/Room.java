package model;

import java.sql.ResultSet;
import java.sql.SQLException;

import static common.Constants.*;

/**
 *
 * Created by Kunmiao Yang on 2/12/2018.
 */
public class Room extends Model {
    private int number, maxOccupy;
    private Hotel hotel;
    private String type;
    private float nightlyRate;
    boolean availability;

    private Room(Hotel hotel, int number) {
        this.hotel = hotel;
        this.number = number;
    }

    public Room(Hotel hotel, int number, String type, boolean availability) throws SQLException {
        this.hotel = hotel;
        this.number = number;
        this.type = type;
        this.availability = availability;
        // Query maxOccupy and nightly rate
        ResultSet resultSet = database.getStatement().executeQuery(
                "SELECT * FROM room_type WHERE room_type = '" + this.type + "';");
        if(!resultSet.next()) throw new SQLException(ERROR_ROOM_INVALID_ROOM_TYPE);
        this.maxOccupy = resultSet.getInt("max_occupancy");
        this.nightlyRate = resultSet.getInt("nightly_rate");

        // Create tuple in database
        database.getStatement().executeUpdate("INSERT INTO " +
                "room(hotel_id, room_number, room_type, availability)" +
                "VALUES (" + this.hotel.getId() + ", " + this.number + ", '" + this.type + "', " + (availability?1:0) + ");");
    }

    public static Room getById(int hotelId, int roomNumber) {
        // Get instance from database
        Room room = new Room(Hotel.getById(hotelId), roomNumber);
        try {
            // Query room info
            ResultSet resultSet = database.getStatement().executeQuery(
                    "SELECT * FROM room WHERE hotel_id = " + hotelId + " AND room_number = " + roomNumber + ";");
            if(!resultSet.next()) return null;
            room.setType(resultSet.getString("room_type"));
            room.setAvailability(resultSet.getInt("availability") == 1);
            resultSet.close();

            // Query room type info
            resultSet = database.getStatement().executeQuery(
                    "SELECT * FROM room_type WHERE room_type = '" + room.type + "';"
            );
            if(!resultSet.next()) return null;
            room.setMaxOccupy(resultSet.getInt("max_occupancy"));
            room.setNightlyRate(resultSet.getInt("nightly_rate"));
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return room;
    }

    public void remove() throws SQLException {
        // Remove from DB
        remove(TABLE_ROOM, "hotel_id = " + this.hotel.getId() + " AND room_number = " + this.number);
    }

    public void update() throws SQLException {
        // Update attributes to DB
        database.getStatement().executeUpdate("UPDATE room " +
                "SET room_type = '" + this.type + "'" +
                ", availability = " + (this.availability?1:0) +
                " WHERE hotel_id = " + this.hotel.getId() + " AND room_number = " + this.number + ";");
    }

    public CheckIn getCurrentCheckIn() {
        // Get current check in
        if(availability) return null;
        try {
            ResultSet resultSet = database.getStatement().executeQuery("SELECT * FROM checkin" +
                    " WHERE hotel_id = " + hotel.getId() +
                    " AND room_number = " + number +
                    " ORDER BY checkin_time DESC;");
            if(!resultSet.next()) return null;
            int checkinId = resultSet.getInt("checkin_id");
            resultSet.close();
            return CheckIn.getById(checkinId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public int getMaxOccupy() {
        return maxOccupy;
    }

    public void setMaxOccupy(int maxOccupy) {
        this.maxOccupy = maxOccupy;
    }

    public float getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(float nightlyRate) {
        this.nightlyRate = nightlyRate;
    }
}
