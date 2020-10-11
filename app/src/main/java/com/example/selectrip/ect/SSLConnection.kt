package com.example.selectrip.ect

import android.content.Context
import com.example.selectrip.R
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

//Flask server와 Https 통신을 하기 위한 클래스
class SSLConnection {

    fun getSocketFactory(context : Context): SSLSocketFactory {
        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        val caInput: InputStream = context.resources.openRawResource(R.raw.ca)
        val ca: X509Certificate = caInput.use {
            cf.generateCertificate(it) as X509Certificate
        }

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType).apply {
            load(null, null)
            setCertificateEntry("ca", ca)
        }

        // Create a TrustManager that trusts the CAs inputStream our KeyStore
        val tmfAlgorithm: String = TrustManagerFactory.getDefaultAlgorithm()
        val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm).apply {
            init(keyStore)
        }

        // Create an SSLContext that uses our TrustManager
        val sslContext: SSLContext = SSLContext.getInstance("TLS")
        sslContext.init(null, tmf.trustManagers, null)

        return sslContext.socketFactory
    }
}


class HostVerifier : HostnameVerifier {
    override fun verify(p0: String?, p1: SSLSession?): Boolean {
        return true
    }
}