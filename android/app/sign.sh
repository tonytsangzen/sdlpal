#jarsigner -verbose -keystore ../../keystore/tony -storepass 123456 -signedjar chatKid.apk -digestalg SHA1 -sigalg MD5withRSA $1 test
/Users/cengtao/Library/Android/sdk//build-tools/31.0.0/apksigner sign --ks ~/work/keystore/tony --ks-key-alias test $1
