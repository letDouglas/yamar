package com.yamar.userservice.dto;

import com.yamar.userservice.model.AddressType;
import lombok.Data;

@Data
public class AddressRequest {
    private String street;
    private String city;
    private String zipCode;
    private String country;
    private AddressType type;
}