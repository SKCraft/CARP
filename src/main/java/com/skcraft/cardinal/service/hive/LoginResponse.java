package com.skcraft.cardinal.service.hive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LoginResponse {
    private boolean accepted;
    private String rejectMessage;
    private User user;
}
