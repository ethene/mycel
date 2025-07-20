package com.quantumresearch.mycel.infrastructure.crypto;

import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;

interface PasswordBasedKdf {

	int chooseCostParameter();

	SecretKey deriveKey(String password, byte[] salt, int cost);
}
