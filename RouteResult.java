/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Myclasses;
public class RouteResult {
        private int stationId;
        private int distance;
        private Route route;

        public RouteResult(int stationId, int distance, Route route) {
            this.stationId = stationId;
            this.distance = distance;
            this.route = route;
        }

        public int getStationId() {
            return stationId;
        }

        public int getDistance() {
            return distance;
        }

        public Route getRoute() {
            return route;
        }

    public int getTrainId() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    }


