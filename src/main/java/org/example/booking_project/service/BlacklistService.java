package org.example.booking_project.service;

import org.example.booking_project.Dtos.BlacklistedDTO;

import java.io.IOException;
import java.net.MalformedURLException;

public interface BlacklistService {
    public void updateBlacklisted(BlacklistedDTO blacklistedDTO);
    public BlacklistedDTO getBlacklistedByEmail(String email) throws IOException;

    public boolean existsByEmail(String email) throws IOException;

    public BlacklistedDTO[] getBlacklistedArrayFromSource() throws IOException;

}
