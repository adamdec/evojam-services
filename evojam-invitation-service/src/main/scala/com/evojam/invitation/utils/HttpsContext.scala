package com.evojam.invitation.utils

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}

import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http}
import com.evojam.invitation.config.Configuration.SslConfig
import com.typesafe.scalalogging.LazyLogging
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

// $COVERAGE-OFF$

case class HttpsContext(config: SslConfig) extends LazyLogging {

  def enableConditionally(implicit system: ActorSystem): Unit = {
    if (config.enabled) {
      Http().setDefaultServerHttpContext(getHttpsConnectionContext)
      logger.debug("Enabled SSL support")
    }
  }

  def getHttpsConnectionContext: ConnectionContext = {
    val password: Array[Char] = config.password.toCharArray

    val ks: KeyStore = KeyStore.getInstance("PKCS12")
    val keystore: InputStream =
      getClass.getClassLoader.getResourceAsStream(config.certificate)

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory =
      KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    ConnectionContext.https(sslContext)
  }
}

// $COVERAGE-ON$
