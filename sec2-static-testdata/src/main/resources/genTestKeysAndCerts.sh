#!/bin/sh

# Config
key_length=2048
openssl_config="openSSL.conf"
prefix="certificates/sec2"
sv_name="server"
cl_name="client"
cl2_name="client2"
cl3_name="client3"
cert_valid_days=3650
sv_sign_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2Signature/organizationalUnitName=keyserver.sec2.org"
sv_enc_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2KeyEncapsulation/organizationalUnitName=keyserver.sec2.org"
cl_sign_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2TestClientSignature/emailAddress=user@sec2.org"
cl_enc_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2TestClientKeyEncapsulation/emailAddress=user@sec2.org"
cl2_sign_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2TestAnotherClientSignature/emailAddress=anotheruser@sec2.org"
cl2_enc_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2TestAnotherClientKeyEncapsulation/emailAddress=anotheruser@sec2.org"
cl3_sign_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2TestUserToRegisterSignature/emailAddress=usertoregister@sec2.org"
cl3_enc_subject="/C=DE/O=NDS/localityName=Bochum/commonName=Sec2TestUserToRegisterKeyEncapsulation/emailAddress=usertoregister@sec2.org"

# Set Variables
sv_sign_keyfile="$prefix.$sv_name.sign.key.pem"
sv_sign_key_pkcs8="$prefix.$sv_name.sign.key.pkcs8"
sv_sign_key_base64="$sv_sign_key_pkcs8.base64"
sv_sign_csr="$prefix.$sv_name.sign.csr"
sv_sign_cert="$prefix.$sv_name.sign.crt"
sv_enc_keyfile="$prefix.$sv_name.enc.key.pem"
sv_enc_key_pkcs8="$prefix.$sv_name.enc.key.pkcs8"
sv_enc_key_base64="$sv_enc_key_pkcs8.base64"
sv_enc_csr="$prefix.$sv_name.enc.csr"
sv_enc_cert="$prefix.$sv_name.enc.crt"
cl_sign_keyfile="$prefix.$cl_name.sign.key.pem"
cl_sign_key_pkcs8="$prefix.$cl_name.sign.key.pkcs8"
cl_sign_key_base64="$cl_sign_key_pkcs8.base64"
cl_sign_csr="$prefix.$cl_name.sign.csr"
cl_sign_cert="$prefix.$cl_name.sign.crt"
cl_enc_keyfile="$prefix.$cl_name.enc.key.pem"
cl_enc_key_pkcs8="$prefix.$cl_name.enc.key.pkcs8"
cl_enc_key_base64="$cl_enc_key_pkcs8.base64"
cl_enc_csr="$prefix.$cl_name.enc.csr"
cl_enc_cert="$prefix.$cl_name.enc.crt"
cl2_sign_keyfile="$prefix.$cl2_name.sign.key.pem"
cl2_sign_key_pkcs8="$prefix.$cl2_name.sign.key.pkcs8"
cl2_sign_key_base64="$cl2_sign_key_pkcs8.base64"
cl2_sign_csr="$prefix.$cl2_name.sign.csr"
cl2_sign_cert="$prefix.$cl2_name.sign.crt"
cl2_enc_keyfile="$prefix.$cl2_name.enc.key.pem"
cl2_enc_key_pkcs8="$prefix.$cl2_name.enc.key.pkcs8"
cl2_enc_key_base64="$cl2_enc_key_pkcs8.base64"
cl2_enc_csr="$prefix.$cl2_name.enc.csr"
cl2_enc_cert="$prefix.$cl2_name.enc.crt"
cl3_sign_keyfile="$prefix.$cl3_name.sign.key.pem"
cl3_sign_key_pkcs8="$prefix.$cl3_name.sign.key.pkcs8"
cl3_sign_key_base64="$cl3_sign_key_pkcs8.base64"
cl3_sign_csr="$prefix.$cl3_name.sign.csr"
cl3_sign_cert="$prefix.$cl3_name.sign.crt"
cl3_enc_keyfile="$prefix.$cl3_name.enc.key.pem"
cl3_enc_key_pkcs8="$prefix.$cl3_name.enc.key.pkcs8"
cl3_enc_key_base64="$cl3_enc_key_pkcs8.base64"
cl3_enc_csr="$prefix.$cl3_name.enc.csr"
cl3_enc_cert="$prefix.$cl3_name.enc.crt"

echo "Generating Sec2 test keys"
openssl genrsa -out $sv_sign_keyfile $key_length
openssl genrsa -out $sv_enc_keyfile $key_length
openssl genrsa -out $cl_sign_keyfile $key_length
openssl genrsa -out $cl_enc_keyfile $key_length
openssl genrsa -out $cl2_sign_keyfile $key_length
openssl genrsa -out $cl2_enc_keyfile $key_length
openssl genrsa -out $cl3_sign_keyfile $key_length
openssl genrsa -out $cl3_enc_keyfile $key_length

echo "Generating Sec2 Certificate Requests"
openssl req -new -batch -subj $sv_sign_subject -key $sv_sign_keyfile -out $sv_sign_csr
openssl req -new -batch -subj $sv_enc_subject -key $sv_enc_keyfile -out $sv_enc_csr
openssl req -new -batch -subj $cl_sign_subject -key $cl_sign_keyfile -out $cl_sign_csr
openssl req -new -batch -subj $cl_enc_subject -key $cl_enc_keyfile -out $cl_enc_csr
openssl req -new -batch -subj $cl2_sign_subject -key $cl2_sign_keyfile -out $cl2_sign_csr
openssl req -new -batch -subj $cl2_enc_subject -key $cl2_enc_keyfile -out $cl2_enc_csr
openssl req -new -batch -subj $cl3_sign_subject -key $cl3_sign_keyfile -out $cl3_sign_csr
openssl req -new -batch -subj $cl3_enc_subject -key $cl3_enc_keyfile -out $cl3_enc_csr

echo "Generating Sec2 Certificates"
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $sv_sign_csr -signkey $sv_sign_keyfile -out $sv_sign_cert -extensions sec2_keyserver_sign
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $sv_enc_csr -CA $sv_sign_cert -CAkey $sv_sign_keyfile -out $sv_enc_cert -extensions sec2_keyserver_enc -CAcreateserial
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $cl_sign_csr -signkey $cl_sign_keyfile -out $cl_sign_cert -extensions sec2_client_sign
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $cl_enc_csr -signkey $cl_enc_keyfile -out $cl_enc_cert -extensions sec2_client_enc
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $cl2_sign_csr -signkey $cl2_sign_keyfile -out $cl2_sign_cert -extensions sec2_client_sign
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $cl2_enc_csr -signkey $cl2_enc_keyfile -out $cl2_enc_cert -extensions sec2_client_enc
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $cl3_sign_csr -signkey $cl3_sign_keyfile -out $cl3_sign_cert -extensions sec2_client_sign
openssl x509 -extfile $openssl_config -req -days $cert_valid_days -in $cl3_enc_csr -signkey $cl3_enc_keyfile -out $cl3_enc_cert -extensions sec2_client_enc

echo "Generating PKCS8 files for keys"
openssl pkcs8 -topk8 -inform PEM -outform DER -in $sv_sign_keyfile -nocrypt > $sv_sign_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $sv_enc_keyfile -nocrypt > $sv_enc_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $cl_sign_keyfile -nocrypt > $cl_sign_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $cl_enc_keyfile -nocrypt > $cl_enc_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $cl2_sign_keyfile -nocrypt > $cl2_sign_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $cl2_enc_keyfile -nocrypt > $cl2_enc_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $cl3_sign_keyfile -nocrypt > $cl3_sign_key_pkcs8
openssl pkcs8 -topk8 -inform PEM -outform DER -in $cl3_enc_keyfile -nocrypt > $cl3_enc_key_pkcs8

echo "Base64 encoding keys"
base64 $sv_sign_key_pkcs8 > $sv_sign_key_base64
base64 $sv_enc_key_pkcs8 > $sv_enc_key_base64
base64 $cl_sign_key_pkcs8 > $cl_sign_key_base64
base64 $cl_enc_key_pkcs8 > $cl_enc_key_base64
base64 $cl2_sign_key_pkcs8 > $cl2_sign_key_base64
base64 $cl2_enc_key_pkcs8 > $cl2_enc_key_base64
base64 $cl3_sign_key_pkcs8 > $cl3_sign_key_base64
base64 $cl3_enc_key_pkcs8 > $cl3_enc_key_base64
