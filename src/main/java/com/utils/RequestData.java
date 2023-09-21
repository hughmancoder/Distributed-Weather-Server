package com.utils;

import java.io.BufferedReader;
import java.io.IOException;

public class RequestData {
    public long lamportClock;
    public int contentLength;
    public String requestBody;

    public static RequestData readRequest(BufferedReader in) throws IOException {
        RequestData requestData = new RequestData();
        String header;
        while (!(header = in.readLine()).isEmpty()) {
            if (header.startsWith("X-Lamport-Clock: ")) {
                requestData.lamportClock = Long.parseLong(header.substring("X-Lamport-Clock: ".length()));
            }
            if (header.startsWith("Content-Length: ")) {
                requestData.contentLength = Integer.parseInt(header.substring("Content-Length: ".length()));
            }
        }

        // Read the actual body using the Content-Length value, if any
        if (requestData.contentLength > 0) {
            char[] buffer = new char[requestData.contentLength];
            int bytesRead = in.read(buffer, 0, requestData.contentLength);

            // Ensure all data is read
            while (bytesRead < requestData.contentLength) {
                bytesRead += in.read(buffer, bytesRead, requestData.contentLength - bytesRead);
            }

            requestData.requestBody = new String(buffer);
        }

        return requestData;
    }
}
