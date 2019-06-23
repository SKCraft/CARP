package com.skcraft.cardinal.service.remotecommand;

import com.skcraft.cardinal.profile.ProfileId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Request {
    private ProfileId sender;
    private String message;
    private Map<String, Object> context = new HashMap<>();
}
