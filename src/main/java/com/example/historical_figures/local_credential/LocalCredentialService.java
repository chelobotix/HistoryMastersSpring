package com.example.historical_figures.local_credential;

import com.example.historical_figures.user.UserResponse;

public interface LocalCredentialService {
    UserResponse saveLocal(LocalCredentialCreateRequest localCredentialCreateRequest);
}
