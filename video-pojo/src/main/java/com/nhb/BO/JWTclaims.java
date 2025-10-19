package com.nhb.BO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import  java.util.Map;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JWTclaims {
    private String username;
    //创建时间
    private long created=System.currentTimeMillis();
    public Map<String, Object> getClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", this.username);
        claims.put("created", this.created);
        return claims;
    }
    public JWTclaims(String username){
        this.username=username;
    }
}
