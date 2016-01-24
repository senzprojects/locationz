package com.score.senz;

import com.score.senzc.pojos.Senz;


interface ISenzService {
    // send senz messages to service via this function
    void send(in Senz senz);

    // get registered user via this function
    String getUser();
}
