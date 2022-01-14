package com.myfridge.app.supermarkets; 
public class Address{
    public String streetNumber;
    public String streetName;
    public String municipalitySubdivision;
    public String municipality;
    public String countrySecondarySubdivision;
    public String countrySubdivision;
    public String postalCode;
    public String countryCode;
    public String country;
    public String countryCodeISO3;
    public String freeformAddress;
    public String localName;

    @Override
    public String toString() {
        return "Address{" +
                "streetNumber='" + streetNumber + '\'' +
                ", streetName='" + streetName + '\'' +
                ", municipalitySubdivision='" + municipalitySubdivision + '\'' +
                ", municipality='" + municipality + '\'' +
                ", countrySecondarySubdivision='" + countrySecondarySubdivision + '\'' +
                ", countrySubdivision='" + countrySubdivision + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", country='" + country + '\'' +
                ", countryCodeISO3='" + countryCodeISO3 + '\'' +
                ", freeformAddress='" + freeformAddress + '\'' +
                ", localName='" + localName + '\'' +
                '}';
    }
}
