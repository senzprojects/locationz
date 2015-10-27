package com.score.senz;

import com.score.senz.pojos.User;


interface ISenzService {
    // send senz messages to service via this function
    void sendSenz(in User user);
}
