package com.score.senz;

import com.score.senz.pojos.User;
import com.score.senz.pojos.Senz;


interface ISenzService {
    // send senz messages to service via this function
    void sendSenz(in User user);
    void send(in Senz senz);
}
