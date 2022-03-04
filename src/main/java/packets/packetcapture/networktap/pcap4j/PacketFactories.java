/*_##########################################################################
  _##
  _##  Copyright (C) 2012-2019 Pcap4J.org
  _##
  _##########################################################################
*/

package packets.packetcapture.networktap.pcap4j;

import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * @author Kaito Yamada
 * @since pcap4j 0.9.11
 */
public final class PacketFactories {

//  private static final Logger logger = LoggerFactory.getLogger(PacketFactories.class);
//  private static final PacketFactoryBinder FACTORY_BINDER;

//  static {
//    PacketFactoryBinder factoryBinder = null;
//    try {
//      ServiceLoader<PacketFactoryBinderProvider> loader =
//          ServiceLoader.load(PacketFactoryBinderProvider.class);
//      Iterator<PacketFactoryBinderProvider> iter = loader.iterator();
//      if (iter.hasNext()) {
//        PacketFactoryBinderProvider packetFactoryBinderProvider = new PacketFactoryBinderProvider() {
//          @Override
//          public PacketFactoryBinder getBinder() {
//            return StaticDataLinkTypePacketFactory.getInstance();
//          }
//        };
//        ProtectionDomain pd = packetFactoryBinderProvider.getClass().getProtectionDomain();
//        URL codeSrcLocation = null;
//        if (pd != null) {
//          codeSrcLocation = pd.getCodeSource().getLocation();
//        }
//        System.out.println("1");
////        logger.info(
////            "A PacketFactoryBinderProvider implementation is found. ClassLoader: {}, URL: {}",
////            packetFactoryBinderProvider.getClass().getClassLoader().toString(),
////            codeSrcLocation);
//        factoryBinder = packetFactoryBinderProvider.getBinder();
////        logger.info("Succeeded in PacketFactoryBinderProvider.getBinder()");
//      } else {
//        System.out.println("2");
////        logger.warn(
////            "No PacketFactoryBinder is available. All packets will be captured as UnknownPacket.");
//      }
//    } catch (ServiceConfigurationError e) {
//      System.out.println("3");
////      logger.warn(e.getClass().getName() + ": " + e.getMessage());
//    }
//    FACTORY_BINDER = factoryBinder;
//  }

  private PacketFactories() {
    throw new AssertionError();
  }

  /**
   * @param <T> target
   * @param <N> number
   * @param targetClass targetClass
   * @param numberClass numberClass
   * @return a {@link PacketFactory PacketFactory} object.
   */
  public static <T, N extends NamedNumber<?, ?>> PacketFactory<T, N> getFactory(
      Class<T> targetClass, Class<N> numberClass) {
    if (numberClass == null || targetClass == null) {
      StringBuilder sb = new StringBuilder();
      sb.append("numberClass: ").append(numberClass).append(" targetClass: ").append(targetClass);
      throw new NullPointerException(sb.toString());
    }

    return StaticPacketFactoryBinder.getInstance().getPacketFactory(targetClass, numberClass);
//    if (FACTORY_BINDER != null) {
//      return FACTORY_BINDER.getPacketFactory(targetClass, numberClass);
//    } else {
//      return SimplePacketFactoryBinder.getInstance().getPacketFactory(targetClass, numberClass);
//    }
  }
}
