package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;

interface PasswordBasedKdf {

	int chooseCostParameter();

	SecretKey deriveKey(String password, byte[] salt, int cost);
}
