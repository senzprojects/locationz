package com.score.senz;

import com.score.senz.pojos.Senz;


interface ISenzService {
    // send senz messages to service via this function
    void send(in Senz senz);
}
