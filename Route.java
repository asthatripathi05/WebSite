/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Myclasses;

import java.sql.Time;

public class Route {
    // Helper classes
        private int trainId;
        private int destination;
        private int distance;
        private Time arrivalTime;
        private Time departureTime;

        public Route(int trainId, int destination, int distance, Time arrivalTime, Time departureTime) {
            this.trainId = trainId;
            this.destination = destination;
            this.distance = distance;
            this.arrivalTime = arrivalTime;
            this.departureTime = departureTime;
        }

        public int getTrainId() {
            return trainId;
        }

        public int getDestination() {
            return destination;
        }

        public int getDistance() {
            return distance;
        }

        public Time getArrivalTime() {
            return arrivalTime;
        }

        public Time getDepartureTime() {
            return departureTime;
        }
    }

