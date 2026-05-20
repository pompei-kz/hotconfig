package kz.pompei.hotconfig.etcd;

/**
 * Contains the data necessary to connect to etcd and determine where configuration keys are stored.
 */
public class ConfTunnelEtcdDef {

  /**
   * Prefix under which configuration keys are stored in etcd.
   * <p>
   * The final key is composed as {@code keyPrefix + localPath}.
   */
  public String keyPrefix = "/kz-pompei-conf-etcd/";
}
