class SSLTrustCertLibrary:
        ROBOT_LIBRARY_SCOPE = 'GLOBAL'
        
        def trustAllCertificate(self):
                from javax.net.ssl import TrustManager, X509TrustManager
                from jarray import array
                from javax.net.ssl import SSLContext
                
                class TrustAllX509TrustManager(X509TrustManager):
                        def checkClientTrusted(self, chain, auth):
                                pass
                        def checkServerTrusted(self, chain, auth):
                                pass
                        def getAcceptedIssuers(self):
                                return None
                
                trust_managers = array([TrustAllX509TrustManager()], TrustManager)
                TRUST_ALL_CONTEXT = SSLContext.getInstance("SSL")
                TRUST_ALL_CONTEXT.init(None, trust_managers, None)
                SSLContext.setDefault(TRUST_ALL_CONTEXT)
                
        def trustSpecificCertificate(self, pemCertificateFile, pemCertificateAlias):
                from java.io import BufferedInputStream, FileInputStream
                from java.security import KeyStore
                from java.security.cert import CertificateFactory, X509Certificate
                from javax.net.ssl import SSLContext, TrustManagerFactory
                
                fis = FileInputStream(pemCertificateFile)
                bis = BufferedInputStream(fis)
                ca = CertificateFactory.getInstance("X.509").generateCertificate(bis)
                ks = KeyStore.getInstance(KeyStore.getDefaultType())
                ks.load(None, None)
                ks.setCertificateEntry(pemCertificateAlias, ca)
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                tmf.init(ks)
                context = SSLContext.getInstance("SSL")
                context.init(None, tmf.getTrustManagers(), None)
                SSLContext.setDefault(context)