package edu.uky.irnc.streamserver.sresource;

import java.lang.reflect.Field;

@SuppressWarnings({"all"})
public class netFlow {
    private String tcp_flags;
    private String src_as_path;
    private String stamp_updated;
    private String cos;
    private String etype;
    private String tag2;
    private String timestamp_end;
    private String vlan;
    private String peer_as_src;
    private long bytes;
    private String tag;
    private String ip_dst;
    private String as_path;
    private String peer_ip_src;
    private String label;
    private String as_dst;
    private String port_dst;
    private String src_comms;
    private String med;
    private String nat_event;
    private String mac_dst;
    private String comms;
    private String as_src;
    private String peer_ip_dst;
    private String iface_out;
    private String mac_src;
    private String src_med;
    private String peer_as_dst;
    private String src_local_pref;
    private String local_pref;
    private String post_nat_port_src;
    private String iface_in;
    private String mpls_vpn_rd;
    private String ip_src;
    private String post_nat_ip_dst;
    private String ip_proto;
    private String mask_src;
    private String mask_dst;
    private String port_src;
    private String tos;
    private String sampling_rate;
    private String post_nat_ip_src;
    private String post_nat_port_dst;
    private String stamp_inserted;
    private String mpls_label_top;
    private String mpls_label_bottom;
    private String mpls_stack_depth;
    private String packets;
    private String timestamp_start;
    private String flows;

    public netFlow(String tcp_flags, String src_as_path, String stamp_updated, String cos, String etype, String tag2, String timestamp_end, String vlan, String peer_as_src, String bytes, String tag, String ip_dst, String as_path, String peer_ip_src, String label, String as_dst, String port_dst, String src_comms, String med, String nat_event, String mac_dst, String comms, String as_src, String peer_ip_dst, String iface_out, String mac_src, String src_med, String peer_as_dst, String src_local_pref, String local_pref, String post_nat_port_src, String iface_in, String mpls_vpn_rd, String ip_src, String post_nat_ip_dst, String ip_proto, String mask_src, String mask_dst, String port_src, String tos, String sampling_rate, String post_nat_ip_src, String post_nat_port_dst, String stamp_inserted, String mpls_label_top, String mpls_label_bottom, String mpls_stack_depth, String packets, String timestamp_start, String flows) {
        this.tcp_flags = tcp_flags;
        this.src_as_path = src_as_path;
        this.stamp_updated = stamp_updated;
        this.cos = cos;
        this.etype = etype;
        this.tag2 = tag2;
        this.timestamp_end = timestamp_end;
        this.vlan = vlan;
        this.peer_as_src = peer_as_src;
        this.bytes = Long.parseLong(bytes);
        this.tag = tag;
        this.ip_dst = ip_dst;
        this.as_path = as_path;
        this.peer_ip_src = peer_ip_src;
        this.label = label;
        this.as_dst = as_dst;
        this.port_dst = port_dst;
        this.src_comms = src_comms;
        this.med = med;
        this.nat_event = nat_event;
        this.mac_dst = mac_dst;
        this.comms = comms;
        this.as_src = as_src;
        this.peer_ip_dst = peer_ip_dst;
        this.iface_out = iface_out;
        this.mac_src = mac_src;
        this.src_med = src_med;
        this.peer_as_dst = peer_as_dst;
        this.src_local_pref = src_local_pref;
        this.local_pref = local_pref;
        this.post_nat_port_src = post_nat_port_src;
        this.iface_in = iface_in;
        this.mpls_vpn_rd = mpls_vpn_rd;
        this.ip_src = ip_src;
        this.post_nat_ip_dst = post_nat_ip_dst;
        this.ip_proto = ip_proto;
        this.mask_src = mask_src;
        this.mask_dst = mask_dst;
        this.port_src = port_src;
        this.tos = tos;
        this.sampling_rate = sampling_rate;
        this.post_nat_ip_src = post_nat_ip_src;
        this.post_nat_port_dst = post_nat_port_dst;
        this.stamp_inserted = stamp_inserted;
        this.mpls_label_top = mpls_label_top;
        this.mpls_label_bottom = mpls_label_bottom;
        this.mpls_stack_depth = mpls_stack_depth;
        this.packets = packets;
        this.timestamp_start = timestamp_start;
        this.flows = flows;
    }

    public long getBytes() {
        return bytes;
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();

        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = this.getClass().getDeclaredFields();

        result.append("{");
        //print field names paired with their values
        try {
            for (int i = 0; i < fields.length - 1; i++) {
                result.append("\"");
                result.append(fields[i].getName());
                result.append("\":\"");
                //requires access to private field:
                result.append(fields[i].get(this));
                result.append("\",");
            }
            result.append("\"");
            result.append(fields[fields.length - 1].getName());
            result.append("\":\"");
            //requires access to private field:
            result.append(fields[fields.length - 1].get(this));
            result.append("\"}");
        } catch (IllegalAccessException ex) {
            System.out.println(ex);
        }
        return result.toString();
    }
}
