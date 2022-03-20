/*
 * Copyright (c) 2020-2021 Pcap Project
 * SPDX-License-Identifier: MIT OR Apache-2.0
 */
package packets.packetcapture.sniff.ardikars;

import com.sun.jna.Callback;
import com.sun.jna.FromNativeContext;
import com.sun.jna.FunctionMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import pcap.common.logging.Logger;
import pcap.common.logging.LoggerFactory;
import pcap.jdk7.internal.NativeSignature;
import pcap.spi.Address;
import pcap.spi.Interface;
import pcap.spi.Timestamp;
import pcap.spi.annotation.Version;

/**
 * Directly extracted out of ardikars library to make edits possible.
 * https://github.com/ardikars/pcap
 */
public class NativeMappings {

    static final int RESTRICTED_LEVEL;
    static final int RESTRICTED_LEVEL_DENY = 0;
    static final int RESTRICTED_LEVEL_PERMIT = 1;
    static final int RESTRICTED_LEVEL_WARN = 2;
    static final String RESTRICTED_MESSAGE =
            "Access to restricted method is disabled by default; to enabled access to restricted method, the Pcap property 'pcap.restricted' must be set to a value other then deny.";
    static final String RESTRICTED_PROPERTY_VALUE =
            "The possible values for this property are:\n"
                    + "0) deny: issues a runtime exception on each restricted call. This is the default value;\n"
                    + "1) permit: allows restricted calls;\n"
                    + "2) warn: like permit, but also prints a one-line warning on each restricted call.\n";
    static final int OK = 0;
    static final int TRUE = 1;
    static final int FALSE = 0;
    static final short AF_INET;
    static final short AF_INET6;
    static final DefaultPlatformDependent PLATFORM_DEPENDENT;
    static final boolean IS_WIN_PCAP;
    private static final Logger LOG = LoggerFactory.getLogger(NativeMappings.class);
    private static final Map<String, Object> NATIVE_LOAD_LIBRARY_OPTIONS =
            new HashMap<String, Object>();

    static {
        com.sun.jna.Native.register(NativeMappings.class, NativeLibrary.getInstance(libName(Platform.isWindows())) );

        // for interface mapping
        final Map<String, String> funcMap = new HashMap<String, String>();
        funcMap.put("pcap_dump_open_append", "pcap_dump_open_append");
        funcMap.put("pcap_get_tstamp_precision", "pcap_get_tstamp_precision");
        funcMap.put("pcap_set_tstamp_type", "pcap_set_tstamp_type");
        funcMap.put("pcap_set_rfmon", "pcap_set_rfmon");
        funcMap.put(
                "pcap_open_offline_with_tstamp_precision", "pcap_open_offline_with_tstamp_precision");
        funcMap.put("pcap_set_tstamp_precision", "pcap_set_tstamp_precision");
        funcMap.put("pcap_set_immediate_mode", "pcap_set_immediate_mode");
        funcMap.put("pcap_setmintocopy", "pcap_setmintocopy");
        funcMap.put("pcap_get_selectable_fd", "pcap_get_selectable_fd");
        funcMap.put("pcap_get_required_select_timeout", "pcap_get_required_select_timeout");
        funcMap.put("pcap_getevent", "pcap_getevent");
        funcMap.put("pcap_statustostr", "pcap_statustostr");
        funcMap.put("pcap_inject", "pcap_inject");
        funcMap.put("pcap_dump_ftell", "pcap_dump_ftell");
        funcMap.put("pcap_setdirection", "pcap_setdirection");
        funcMap.put("pcap_create", "pcap_create");
        funcMap.put("pcap_set_snaplen", "pcap_set_snaplen");
        funcMap.put("pcap_set_promisc", "pcap_set_promisc");
        funcMap.put("pcap_set_timeout", "pcap_set_timeout");
        funcMap.put("pcap_set_buffer_size", "pcap_set_buffer_size");
        funcMap.put("pcap_activate", "pcap_activate");

        NATIVE_LOAD_LIBRARY_OPTIONS.put(
                Library.OPTION_FUNCTION_MAPPER,
                new FunctionMapper() {
                    @Override
                    public String getFunctionName(NativeLibrary library, Method method) {
                        return funcMap.get(method.getName());
                    }
                });
        PLATFORM_DEPENDENT = new DefaultPlatformDependent();
        IS_WIN_PCAP = pcap_lib_version().toLowerCase().contains("winpcap");

        AF_INET = 2;
        AF_INET6 = defaultAfInet6();

        String characterEncoding = System.getProperty("pcap.character.encoding");
        initLibrary(characterEncoding);
        String unsafeAccess = System.getProperty("pcap.restricted", "deny");
        if (unsafeAccess.equals("deny")) {
            RESTRICTED_LEVEL = RESTRICTED_LEVEL_DENY;
        } else if (unsafeAccess.equals("permit")) {
            RESTRICTED_LEVEL = RESTRICTED_LEVEL_PERMIT;
        } else if (unsafeAccess.equals("warn")) {
            RESTRICTED_LEVEL = RESTRICTED_LEVEL_WARN;
        } else {
            RESTRICTED_LEVEL = RESTRICTED_LEVEL_DENY;
        }
    }

    private NativeMappings() {}

    static void initLibrary(String characterEncoding) {
        if (characterEncoding != null) {
            /*
             * Initialization options.
             * All bits not listed here are reserved for expansion.
             *
             * On UNIX-like systems, the local character encoding is assumed to be
             * UTF-8, so no character encoding transformations are done.
             *
             * On Windows, the local character encoding is the local ANSI code page.
             */
            int PCAP_CHAR_ENC_LOCAL = 0x00000000; /* strings are in the local character encoding */
            int PCAP_CHAR_ENC_UTF_8 = 0x00000001; /* strings are in UTF-8 */
            int rc;
            ErrorBuffer errbuf = new ErrorBuffer();
            errbuf.clear();
            errbuf.buf[0] = '\0';
            if (characterEncoding.equals("UTF-8")) {
                rc = PLATFORM_DEPENDENT.pcap_init(PCAP_CHAR_ENC_UTF_8, errbuf);
            } else {
                rc = PLATFORM_DEPENDENT.pcap_init(PCAP_CHAR_ENC_LOCAL, errbuf);
            }
            eprint(rc, errbuf);
        }
    }

    static void eprint(int rc, ErrorBuffer errbuf) {
        if (rc != 0) {
            LOG.warn(errbuf.toString());
        }
    }

    static String libName(boolean isWindows) {
        if (isWindows) {
            return "wpcap";
        } else {
            return "pcap";
        }
    }

    static short defaultAfInet6() {
        short af_inet6 = 0;
        String afInet6 = System.getProperty("pcap.af.inet6");
        try {
            af_inet6 = (short) Integer.parseInt(afInet6);
        } catch (NumberFormatException e) {
            switch (Platform.getOSType()) {
                case Platform.MAC:
                    af_inet6 = 30;
                    break;
                case Platform.KFREEBSD:
                    af_inet6 = 28;
                    break;
                case Platform.LINUX:
                    af_inet6 = 10;
                    break;
                default:
                    af_inet6 = 23;
            }
        }
        return af_inet6;
    }

    @NativeSignature(
            signature = "const char *pcap_lib_version(void)",
            since = @Version(major = 0, minor = 8))
    static native String pcap_lib_version();

    @NativeSignature(
            signature = "char *pcap_geterr(pcap_t *p)",
            since = @Version(major = 0, minor = 4))
    static native Pointer pcap_geterr(Pointer p);

    @NativeSignature(
            signature = "void pcap_freealldevs(pcap_if_t *alldevs)",
            since = @Version(major = 0, minor = 7))
    static native int pcap_findalldevs(PointerByReference alldevsp, ErrorBuffer errbuf);

    @NativeSignature(
            signature = "void pcap_freealldevs(pcap_if_t *alldevs)",
            since = @Version(major = 0, minor = 7))
    static native void pcap_freealldevs(Pointer p);

    @NativeSignature(
            signature = "pcap_t *pcap_open_offline(const char *fname, char *errbuf)",
            since = @Version(major = 0, minor = 4))
    static native Pointer pcap_open_live(
            String device, int snaplen, int promisc, int toMs, ErrorBuffer errbuf);

    @NativeSignature(
            signature = "pcap_t *pcap_open_offline(const char *fname, char *errbuf)",
            since = @Version(major = 0, minor = 4))
    static native Pointer pcap_open_offline(String fname, ErrorBuffer errbuf);

    @NativeSignature(
            signature = "int pcap_loop(pcap_t *p, int cnt, pcap_handler callback, u_char *user)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_loop(Pointer p, int cnt, pcap_handler callback, Pointer user);

    @NativeSignature(
            signature = "int pcap_dispatch(pcap_t *p, int cnt, pcap_handler callback, u_char *user)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_dispatch(Pointer p, int cnt, pcap_handler callback, Pointer user);

    @NativeSignature(
            signature = "int pcap_sendpacket(pcap_t *p, const u_char *buf, int size)",
            since = @Version(major = 0, minor = 8))
    static native int pcap_sendpacket(Pointer p, Pointer buf, int size);

    @NativeSignature(
            signature =
                    "int pcap_compile(pcap_t *p, struct bpf_program *fp, const char *str, int optimize, bpf_u_int32 netmask)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_compile(Pointer p, bpf_program fp, String str, int optimize, int netmask);

    @NativeSignature(
            signature = "int pcap_setfilter(pcap_t *p, struct bpf_program *fp)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_setfilter(Pointer p, bpf_program fp);

    @NativeSignature(
            signature = "void pcap_freecode(struct bpf_program *)",
            since = @Version(major = 0, minor = 6))
    static native void pcap_freecode(bpf_program fp);

    @NativeSignature(signature = "void pcap_close(pcap_t *p)", since = @Version(major = 0, minor = 4))
    static native void pcap_close(Pointer p);

    @NativeSignature(signature = "pcap_breakloop", since = @Version(major = 0, minor = 8))
    static native void pcap_breakloop(Pointer p);

    @NativeSignature(
            signature =
                    "int pcap_next_ex(pcap_t *p, struct pcap_pkthdr **pkt_header, const u_char **pkt_data)",
            since = @Version(major = 0, minor = 8))
    static native int pcap_next_ex(Pointer p, PointerByReference h, PointerByReference data);

    @NativeSignature(
            signature = "pcap_dumper_t *pcap_dump_open(pcap_t *p, const char *fname)",
            since = @Version(major = 0, minor = 4))
    static native Pointer pcap_dump_open(Pointer p, String fname);

    @NativeSignature(
            signature = "void pcap_dump(u_char *user, struct pcap_pkthdr *h, u_char *sp)",
            since = @Version(major = 0, minor = 4))
    static native void pcap_dump(Pointer user, Pointer header, Pointer packet);

    @NativeSignature(
            signature = "int pcap_dump_flush(pcap_dumper_t *p)",
            since = @Version(major = 0, minor = 8))
    static native int pcap_dump_flush(Pointer p);

    @NativeSignature(
            signature = "void pcap_dump_close(pcap_dumper_t *p)",
            since = @Version(major = 0, minor = 4))
    static native void pcap_dump_close(Pointer p);

    @NativeSignature(
            signature = "int pcap_stats(pcap_t *p, struct pcap_stat *ps)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_stats(Pointer p, Pointer ps);

    @NativeSignature(
            signature = "int pcap_setnonblock(pcap_t *p, int nonblock, char *errbuf)",
            since = @Version(major = 0, minor = 7))
    static native int pcap_setnonblock(Pointer p, int nonblock, ErrorBuffer errbuf);

    @NativeSignature(
            signature = "int pcap_getnonblock(pcap_t *p, char *errbuf)",
            since = @Version(major = 0, minor = 7))
    static native int pcap_getnonblock(Pointer p, ErrorBuffer errbuf);

    @NativeSignature(
            signature = "int pcap_minor_version(pcap_t *p)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_minor_version(Pointer p);

    @NativeSignature(
            signature = "int pcap_snapshot(pcap_t *p)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_snapshot(Pointer p);

    @NativeSignature(
            signature = "int pcap_major_version(pcap_t *p)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_major_version(Pointer p);

    @NativeSignature(
            signature = "int pcap_is_swapped(pcap_t *p)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_is_swapped(Pointer p);

    @NativeSignature(
            signature = "int pcap_datalink(pcap_t *p)",
            since = @Version(major = 0, minor = 4))
    static native int pcap_datalink(Pointer p);

    @NativeSignature(
            signature = "u_int bpf_filter(const struct bpf_insn *, const u_char *, u_int, u_int)",
            since = @Version(major = 0, minor = 4))
    static native int bpf_filter(
            bpf_insn insn, Pointer packet, int oriPktLen, int pktLen);

    static InetAddress inetAddress(sockaddr sockaddr) {
        if (sockaddr == null) {
            return null;
        }
        InetAddress address;
        try {
            if (sockaddr.getSaFamily() == AF_INET) {
                address = InetAddress.getByAddress(Arrays.copyOfRange(sockaddr.sa_data, 2, 6));
            } else if (sockaddr.sa_family == AF_INET6) {
                address = InetAddress.getByAddress(Arrays.copyOfRange(sockaddr.sa_data, 2, 18));
            } else {
                address = null;
            }
        } catch (Exception e) {
            address = null;
        }
        return address;
    }

    interface pcap_handler extends Callback {

        void got_packet(Pointer args, Pointer header, Pointer packet);
    }

    interface PlatformDependent extends Library {

        @NativeSignature(
                signature = "int pcap_inject(pcap_t *p, const void *buf, size_t size)",
                since = @Version(major = 0, minor = 9))
        int pcap_inject(Pointer p, Pointer buf, int size);

        @NativeSignature(
                signature = "long pcap_dump_ftell(pcap_dumper_t *p)",
                since = @Version(major = 0, minor = 9))
        NativeLong pcap_dump_ftell(Pointer dumper);

        @NativeSignature(
                signature = "int pcap_setdirection(pcap_t *p, pcap_direction_t d)",
                since = @Version(major = 0, minor = 9))
        int pcap_setdirection(Pointer p, int pcap_direction);

        @NativeSignature(
                signature = "pcap_t *pcap_create(const char *source, char *errbuf)",
                since = @Version(major = 1, minor = 0))
        Pointer pcap_create(String device, ErrorBuffer errbuf);

        @NativeSignature(
                signature = "int pcap_set_snaplen(pcap_t *p, int snaplen)",
                since = @Version(major = 1, minor = 0))
        int pcap_set_snaplen(Pointer p, int snaplen);

        @NativeSignature(
                signature = "int pcap_set_promisc(pcap_t *p, int promisc)",
                since = @Version(major = 1, minor = 0))
        int pcap_set_promisc(Pointer p, int promisc);

        @NativeSignature(
                signature = "int pcap_set_timeout(pcap_t *p, int to_ms)",
                since = @Version(major = 1, minor = 0))
        int pcap_set_timeout(Pointer p, int timeout);

        @NativeSignature(
                signature = "int pcap_set_buffer_size(pcap_t *p, int buffer_size)",
                since = @Version(major = 1, minor = 0))
        int pcap_set_buffer_size(Pointer p, int bufferSize);

        @NativeSignature(
                signature = "int pcap_activate(pcap_t *p)",
                since = @Version(major = 1, minor = 0))
        int pcap_activate(Pointer p);

        @NativeSignature(
                signature = "int pcap_can_set_rfmon(pcap_t *p)",
                since = @Version(major = 1, minor = 0))
        int pcap_can_set_rfmon(Pointer p);

        @NativeSignature(
                signature = "const char *pcap_statustostr(int error)",
                since = @Version(major = 1, minor = 0))
        String pcap_statustostr(int error);

        @NativeSignature(
                signature = "pcap_dumper_t *pcap_dump_open_append(pcap_t *p, const char *fname)",
                since = @Version(major = 1, minor = 7))
        Pointer pcap_dump_open_append(Pointer p, String fname);

        @NativeSignature(
                signature = "int pcap_set_tstamp_type(pcap_t *p, int tstamp_type)",
                since = @Version(major = 1, minor = 2))
        int pcap_set_tstamp_type(Pointer p, int tstampType);

        @NativeSignature(
                signature = "int pcap_get_tstamp_precision(pcap_t *p)",
                since = @Version(major = 1, minor = 5))
        int pcap_get_tstamp_precision(Pointer p);

        @NativeSignature(
                signature = "int pcap_set_rfmon(pcap_t *p, int rfmon)",
                since = @Version(major = 1, minor = 0))
        int pcap_set_rfmon(Pointer p, int rfmon);

        @NativeSignature(
                signature =
                        "pcap_t *pcap_open_offline_with_tstamp_precision(const char *fname, u_int precision, char *errbuf)",
                since = @Version(major = 1, minor = 5))
        Pointer pcap_open_offline_with_tstamp_precision(
                String fname, int precision, ErrorBuffer errbuf);

        @NativeSignature(
                signature = "int pcap_set_tstamp_precision(pcap_t *p, int tstamp_precision)",
                since = @Version(major = 1, minor = 5))
        int pcap_set_tstamp_precision(Pointer p, int tstamp_precision);

        @NativeSignature(
                signature = "int pcap_set_immediate_mode(pcap_t *p, int immediate_mode)",
                since = @Version(major = 1, minor = 5))
        int pcap_set_immediate_mode(Pointer p, int immediate_mode);

        @NativeSignature(
                signature = "int pcap_get_selectable_fd(pcap_t *p)",
                since = @Version(major = 0, minor = 8),
                description = "Only available on Unix system.",
                portable = false)
        int pcap_get_selectable_fd(Pointer p);

        @NativeSignature(
                signature = "int pcap_get_required_select_timeout(pcap_t *p)",
                since = @Version(major = 1, minor = 9),
                description = "Only available on Unix system.",
                portable = false)
        Pointer pcap_get_required_select_timeout(Pointer p);

        @NativeSignature(
                signature = "HANDLE pcap_getevent(pcap_t *p)",
                since = @Version(major = 0, minor = 4),
                description = "Only available on Windows system.",
                portable = false)
        HANDLE pcap_getevent(Pointer p);

        @NativeSignature(
                signature = "int pcap_setmintocopy(pcap_t *p, int size)",
                since = @Version(major = 0, minor = 4),
                description = "Only available on Windows system.",
                portable = false)
        int pcap_setmintocopy(Pointer p, int size);

        @NativeSignature(
                signature = "int pcap_init(unsigned int opts, char *errbuf)",
                since = @Version(major = 1, minor = 10),
                description = "Used to initialize the Packet Capture library")
        int pcap_init(int opts, ErrorBuffer errbuf);
    }

    static final class DefaultPlatformDependent implements PlatformDependent {

        private static final NativeLong ZERO = new NativeLong(0);

        private static final PlatformDependent NATIVE =
                com.sun.jna.Native.load(
                        libName(Platform.isWindows()), PlatformDependent.class, NATIVE_LOAD_LIBRARY_OPTIONS);

        private final AtomicBoolean injectIsSupported = new AtomicBoolean(true);
        private final AtomicBoolean dumpFtellIsSupported = new AtomicBoolean(true);

        @Override
        public int pcap_inject(Pointer p, Pointer buf, int size) {
            if (injectIsSupported.get()) {
                try {
                    return NATIVE.pcap_inject(p, buf, size);
                } catch (NullPointerException | UnsatisfiedLinkError e) {
                    LOG.warn("pcap_inject: Function doesn't exist, use pcap_sendpacket.");
                    injectIsSupported.compareAndSet(true, false);
                }
            }
            int rc = pcap_sendpacket(p, buf, size);
            if (rc < 0) {
                return rc;
            } else {
                return size;
            }
        }

        @Override
        public NativeLong pcap_dump_ftell(Pointer dumper) {
            if (dumpFtellIsSupported.get()) {
                try {
                    return NATIVE.pcap_dump_ftell(dumper);
                } catch (NullPointerException | UnsatisfiedLinkError e) {
                    LOG.warn("pcap_dump_ftell: Function doesn't exist.");
                    dumpFtellIsSupported.compareAndSet(true, false);
                }
            }
            return ZERO;
        }

        @Override
        public int pcap_setdirection(Pointer p, int pcap_direction) {
            try {
                return NATIVE.pcap_setdirection(p, pcap_direction);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_setdirection: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public Pointer pcap_create(String device, ErrorBuffer errbuf) {
            try {
                return NATIVE.pcap_create(device, errbuf);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_create: Function doesn't exist.");
                return null;
            }
        }

        @Override
        public int pcap_set_snaplen(Pointer p, int snaplen) {
            try {
                return NATIVE.pcap_set_snaplen(p, snaplen);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_snaplen: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_set_promisc(Pointer p, int promisc) {
            try {
                return NATIVE.pcap_set_promisc(p, promisc);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_promisc: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_set_timeout(Pointer p, int timeout) {
            try {
                return NATIVE.pcap_set_timeout(p, timeout);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_timeout: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_set_buffer_size(Pointer p, int bufferSize) {
            try {
                return NATIVE.pcap_set_buffer_size(p, bufferSize);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_buffer_size: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_activate(Pointer p) {
            try {
                return NATIVE.pcap_activate(p);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_activate: Function doesn't exist.");
                return -1;
            }
        }

        @Override
        public int pcap_can_set_rfmon(Pointer p) {
            try {
                return NATIVE.pcap_can_set_rfmon(p);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_can_set_frmon: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public String pcap_statustostr(int error) {
            try {
                return NATIVE.pcap_statustostr(error);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                return "pcap_statustostr: Function doesn't exist.";
            }
        }

        @Override
        public Pointer pcap_dump_open_append(Pointer p, String fname) {
            try {
                return NATIVE.pcap_dump_open_append(p, fname);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_dump_open_append: Function doesn't exist.");
                return null;
            }
        }

        @Override
        public int pcap_set_tstamp_type(Pointer p, int tstampType) {
            try {
                return NATIVE.pcap_set_tstamp_type(p, tstampType);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_tstamp_type: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_get_tstamp_precision(Pointer p) {
            try {
                return NATIVE.pcap_get_tstamp_precision(p);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_get_tstamp_precision: Function doesn't exist.");
                return Timestamp.Precision.MICRO.value();
            }
        }

        @Override
        public int pcap_set_rfmon(Pointer p, int rfmon) {
            try {
                return NATIVE.pcap_set_rfmon(p, rfmon);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_rfmon: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public Pointer pcap_open_offline_with_tstamp_precision(
                String fname, int precision, ErrorBuffer errbuf) {
            try {
                return NATIVE.pcap_open_offline_with_tstamp_precision(fname, precision, errbuf);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_open_offline_with_tstamp_precision: Function doesn't exist.");
                return null;
            }
        }

        @Override
        public int pcap_set_tstamp_precision(Pointer p, int tstamp_precision) {
            try {
                return NATIVE.pcap_set_tstamp_precision(p, tstamp_precision);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_set_tstamp_precision: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_set_immediate_mode(Pointer p, int immediate_mode) {
            try {
                return NATIVE.pcap_set_immediate_mode(p, immediate_mode);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                return 0; // ignore immediate mode for libpcap version before 1.5.0
            }
        }

        @Override
        public int pcap_setmintocopy(Pointer p, int size) {
            try {
                return NATIVE.pcap_setmintocopy(p, size);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_setmintocopy: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_init(int opts, ErrorBuffer errbuf) {
            try {
                return NATIVE.pcap_init(opts, errbuf);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_init: Function doesn't exist.");
                return 0;
            }
        }

        @Override
        public int pcap_get_selectable_fd(Pointer p) {
            try {
                return NATIVE.pcap_get_selectable_fd(p);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                throw new UnsupportedOperationException("pcap_get_selectable_fd: Function doesn't exist.");
            }
        }

        @Override
        public Pointer pcap_get_required_select_timeout(Pointer p) {
            try {
                return NATIVE.pcap_get_required_select_timeout(p);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                LOG.warn("pcap_get_required_select_timeout: Function doesn't exist.");
                return null;
            }
        }

        @Override
        public HANDLE pcap_getevent(Pointer p) {
            try {
                return NATIVE.pcap_getevent(p);
            } catch (NullPointerException | UnsatisfiedLinkError e) {
                throw new UnsupportedOperationException("pcap_getevent: Function doesn't exist.");
            }
        }
    }

    public static final class ErrorBuffer extends Structure {

        public byte[] buf;

        public ErrorBuffer() {
            this(256);
        }

        public ErrorBuffer(int size) {
            this.buf = new byte[size];
        }

        @Override
        protected List<String> getFieldOrder() {
            return Collections.singletonList("buf");
        }

        @Override
        public String toString() {
            return com.sun.jna.Native.toString(buf);
        }
    }

    public static final class bpf_program extends Structure {

        public int bf_len;
        public bpf_insn.ByReference bf_insns;

        public bpf_program() {
            // public constructor
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "bf_len", //
                    "bf_insns" //
            );
        }
    }

    public static class bpf_insn extends Structure {

        public short code;
        public byte jt;
        public byte jf;
        public int k;

        public bpf_insn() {
            // public constructor
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "code", //
                    "jt", //
                    "jf", //
                    "k");
        }

        public static final class ByReference extends bpf_insn implements Structure.ByReference {}
    }

    public static class sockaddr extends Structure {

        private static final ByteOrder NATIVE_BYTE_ORDER = ByteOrder.nativeOrder();

        public short sa_family;
        public byte[] sa_data = new byte[14];

        public sockaddr() {
            // public constructor
        }

        static boolean isLinuxOrWindows() {
            return Platform.isWindows() || Platform.isLinux();
        }

        static short getSaFamilyByByteOrder(short saFamily, ByteOrder bo) {
            if (bo.equals(ByteOrder.BIG_ENDIAN)) {
                return (short) (0xFF & saFamily);
            } else {
                return (short) (0xFF & (saFamily >> 8));
            }
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "sa_family", //
                    "sa_data" //
            );
        }

        public short getSaFamily() {
            if (isLinuxOrWindows()) {
                return sa_family;
            } else {
                return getSaFamilyByByteOrder(sa_family, NATIVE_BYTE_ORDER);
            }
        }

        public static final class ByReference extends sockaddr implements Structure.ByReference {}
    }

    public static class pcap_if extends Structure implements Interface {

        public pcap_if.ByReference next;
        public String name;
        public String description;
        public pcap_addr.ByReference addresses;
        public int flags;

        public pcap_if() {
            // public constructor
        }

        public pcap_if(Pointer pointer) {
            super(pointer);
            read();
        }

        @Override
        public pcap_if next() {
            return next;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public pcap_addr addresses() {
            return addresses;
        }

        @Override
        public int flags() {
            return flags;
        }

        @Override
        public Iterator<Interface> iterator() {
            return new DefaultInterfaceIterator(this);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "next", //
                    "name", //
                    "description", //
                    "addresses", //
                    "flags" //
            );
        }

        public static final class ByReference extends pcap_if implements Structure.ByReference {}
    }

    public static class pcap_addr extends Structure implements Address {

        public pcap_addr.ByReference next;
        public sockaddr.ByReference addr;
        public sockaddr.ByReference netmask;
        public sockaddr.ByReference broadaddr;
        public sockaddr.ByReference dstaddr;

        public pcap_addr() {}

        @Override
        public pcap_addr next() {
            return next;
        }

        @Override
        public InetAddress address() {
            return inetAddress(addr);
        }

        @Override
        public InetAddress netmask() {
            return inetAddress(netmask);
        }

        @Override
        public InetAddress broadcast() {
            return inetAddress(broadaddr);
        }

        @Override
        public InetAddress destination() {
            return inetAddress(dstaddr);
        }

        @Override
        public Iterator<Address> iterator() {
            return new DefaultAddressIterator(this);
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList(
                    "next", //
                    "addr", //
                    "netmask", //
                    "broadaddr", //
                    "dstaddr" //
            );
        }

        public static final class ByReference extends pcap_addr implements Structure.ByReference {}
    }

    public static class HANDLE extends PointerType {

        static HANDLE INVALID_HANDLE_VALUE =
                new HANDLE(Pointer.createConstant(Native.POINTER_SIZE == 8 ? -1 : 0xFFFFFFFFL));

        private boolean immutable;

        public HANDLE() {}

        public HANDLE(Pointer p) {
            super(p);
            immutable = true;
        }

        /** Override to the appropriate object for INVALID_HANDLE_VALUE. */
        @Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            if (HANDLE.INVALID_HANDLE_VALUE.equals(o)) {
                return HANDLE.INVALID_HANDLE_VALUE;
            }
            return o;
        }

        @Override
        public void setPointer(Pointer p) {
            if (immutable) {
                throw new UnsupportedOperationException("immutable reference");
            }
            super.setPointer(p);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HANDLE handle = (HANDLE) o;
            return Pointer.nativeValue(getPointer()) == Pointer.nativeValue(handle.getPointer());
        }

        @Override
        public int hashCode() {
            return Objects.hash(Pointer.nativeValue(getPointer()));
        }

        @Override
        public String toString() {
            return String.valueOf(getPointer());
        }
    }

    public static class pcap_pkthdr extends Structure {

        public static final int TS_OFFSET;
        public static final int CAPLEN_OFFSET;
        public static final int LEN_OFFSET;

        public timeval ts; // struct timeval
        public int caplen; // bpf_u_int32
        public int len; // bpf_u_int32

        static {
            pcap_pkthdr ph = new pcap_pkthdr();
            TS_OFFSET = ph.fieldOffset("ts");
            CAPLEN_OFFSET = ph.fieldOffset("caplen");
            LEN_OFFSET = ph.fieldOffset("len");
        }

        public pcap_pkthdr() {}

        public pcap_pkthdr(Pointer p) {
            super(p);
            read();
        }

        public static class ByReference extends pcap_pkthdr implements Structure.ByReference {}

        @Override
        protected List<String> getFieldOrder() {
            List<String> list = new ArrayList<String>();
            list.add("ts");
            list.add("caplen");
            list.add("len");
            return list;
        }

        static NativeLong getTvSec(Pointer p) {
            return p.getNativeLong(TS_OFFSET + timeval.TV_SEC_OFFSET);
        }

        static NativeLong getTvUsec(Pointer p) {
            return p.getNativeLong(TS_OFFSET + timeval.TV_USEC_OFFSET);
        }

        static int getCaplen(Pointer p) {
            return p.getInt(CAPLEN_OFFSET);
        }

        static int getLen(Pointer p) {
            return p.getInt(LEN_OFFSET);
        }
    }


    public static class timeval extends Structure {

        public static final int TV_SEC_OFFSET;
        public static final int TV_USEC_OFFSET;

        public NativeLong tv_sec; // long
        public NativeLong tv_usec; // long

        static {
            NativeMappings.timeval tv = new NativeMappings.timeval();
            TV_SEC_OFFSET = tv.fieldOffset("tv_sec");
            TV_USEC_OFFSET = tv.fieldOffset("tv_usec");
        }

        public timeval() {}

        @Override
        protected List<String> getFieldOrder() {
            List<String> list = new ArrayList<String>();
            list.add("tv_sec");
            list.add("tv_usec");
            return list;
        }
    }
}

