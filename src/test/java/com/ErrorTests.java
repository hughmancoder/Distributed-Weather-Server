package com;

public class ErrorTests {

}
// TODO: test aggregation server, start, stop and file recovery
// TODO: test error status code

/*
     * 
     * 
     * 
     * The first
     * time weather
     * data is
     * received and
     * the storage
     * file is created,
     * you should return status 201-
     * HTTP_CREATED. If later
     * 
     * uploads (updates) are successful, you should return status 200. (This
     * if a Content Server first connects to the Aggregation Server, then return201
     * as succeed code, then before the content server lost connection, all other
     * succeed response should use 200). Any request other than GET or PUT should
     * return status 400 (note: this is not standard but to simplify your task).
     * Sending no content to the server should cause a 204 status code to be
     * returned. Finally, if the JSON data does not make
     * 
     * sense (incorrect JSON) you may return status code 500 - Internal server
     * error.
     * 
     * Your server will, by default, start on port 4567 but will accept a single
     * command line argument that gives the starting port number. Your server's main
     * method will reside in a file called AggregationServer.java.
     * 
     * Your server is designed to stay current and will remove any items in theJSON
     * that have come from c
     */

}