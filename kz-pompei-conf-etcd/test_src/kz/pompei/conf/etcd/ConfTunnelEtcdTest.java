package kz.pompei.conf.etcd;

import io.etcd.jetcd.Client;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import kz.pompei.conf.core.model.Conf;
import kz.pompei.conf.core.model.ConfParam;
import kz.pompei.conf.etcd.tst_utils.EtcdTestParent;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfTunnelEtcdTest extends EtcdTestParent {

  @Test public void read_keyDoesNotExists() {
    ConfTunnelEtcdDef params = createParams("read_keyDoesNotExists");

    String localPath = "some/folder/read_keyDoesNotExists.hotconf";
    String fullKey = key(params, localPath);

    try (Client client = createClient();
         ConfTunnelEtcd confTunnelEtcd = new ConfTunnelEtcd(client, params)) {
      assertThat(keyExists(client, fullKey)).isFalse();

      //
      //
      Conf conf = confTunnelEtcd.read(localPath);
      //
      //

      assertThat(conf).isNull();
      assertThat(keyExists(client, fullKey)).isFalse();
    }
  }

  @Test public void write__multiline_comments() throws Exception {
    ConfTunnelEtcdDef params = createParams("write__multiline_comments");

    String localPath = "some/folder/write__multiline_comments.hotconf";
    String fullKey = key(params, localPath);

    Conf conf = new Conf();
    conf.confComments.add("config line 1");
    conf.confComments.add("config line 2");
    conf.confComments.add("config line 3");

    ConfParam param0 = new ConfParam();
    param0.comments.add("param0 line 1");
    param0.comments.add("param0 line 2");
    param0.comments.add("param0 line 3");
    param0.name = "param0";
    param0.valueStr = "value0";
    conf.params.add(param0);

    ConfParam param1 = new ConfParam();
    param1.comments.add("param1 line 1");
    param1.comments.add("param1 line 2");
    param1.comments.add("param1 line 3");
    param1.name = "param1";
    param1.valueStr = "value1";
    conf.params.add(param1);

    try (Client client = createClient();
         ConfTunnelEtcd confTunnelEtcd = new ConfTunnelEtcd(client, params)) {

      //
      //
      confTunnelEtcd.write(localPath, conf);
      //
      //

      assertThat(keyExists(client, fullKey)).isTrue();

      String stored = new String(
        client.getKVClient().get(byteSequence(fullKey)).get().getKvs().get(0).getValue().getBytes(),
        StandardCharsets.UTF_8
      );
      String expectedBody = String.join(
        "\n",
        "#config line 1",
        "#config line 2",
        "#config line 3",
        "",
        "#param0 line 1",
        "#param0 line 2",
        "#param0 line 3",
        "param0=value0",
        "",
        "#param1 line 1",
        "#param1 line 2",
        "#param1 line 3",
        "param1=value1"
      );

      assertThat(stored).startsWith("lastModifiedMillis=");
      assertThat(stored.substring(stored.indexOf('\n') + 1)).isEqualTo(expectedBody);

      Conf readConf = confTunnelEtcd.read(localPath);

      assertThat(readConf).isNotNull();
      assertThat(readConf.confComments).isEqualTo(List.of("config line 1", "config line 2", "config line 3"));

      ConfParam readParam0 = readConf.params.get(0);
      ConfParam readParam1 = readConf.params.get(1);

      assertThat(readParam0.name).isEqualTo("param0");
      assertThat(readParam1.name).isEqualTo("param1");
      assertThat(readParam0.valueStr).isEqualTo("value0");
      assertThat(readParam1.valueStr).isEqualTo("value1");
      assertThat(readParam0.comments).isEqualTo(List.of("param0 line 1", "param0 line 2", "param0 line 3"));
      assertThat(readParam1.comments).isEqualTo(List.of("param1 line 1", "param1 line 2", "param1 line 3"));

      deleteKey(client, fullKey);
      assertThat(keyExists(client, fullKey)).isFalse();
    }
  }

  @Test public void read__multiline_comments() throws Exception {
    ConfTunnelEtcdDef params = createParams("read__multiline_comments");

    String localPath = "some/folder/read__multiline_comments.hotconf";
    String fullKey = key(params, localPath);

    String stored = String.join(
      "\n",
      "lastModifiedMillis=1716038400000",
      "#config line 1",
      "#config line 2",
      "#config line 3",
      "",
      "#param0 line 1",
      "#param0 line 2",
      "#param0 line 3",
      "param0=value0",
      "",
      "#param1 line 1",
      "#param1 line 2",
      "#param1 line 3",
      "param1=value1"
    );

    try (Client client = createClient();
         ConfTunnelEtcd confTunnelEtcd = new ConfTunnelEtcd(client, params)) {
      client.getKVClient().put(byteSequence(fullKey), byteSequence(stored)).get();

      //
      //
      Conf readConf = confTunnelEtcd.read(localPath);
      //
      //

      assertThat(readConf).isNotNull();
      assertThat(readConf.confComments).isEqualTo(List.of("config line 1", "config line 2", "config line 3"));

      ConfParam readParam0 = readConf.params.get(0);
      ConfParam readParam1 = readConf.params.get(1);

      assertThat(readParam0.name).isEqualTo("param0");
      assertThat(readParam1.name).isEqualTo("param1");
      assertThat(readParam0.valueStr).isEqualTo("value0");
      assertThat(readParam1.valueStr).isEqualTo("value1");
      assertThat(readParam0.comments).isEqualTo(List.of("param0 line 1", "param0 line 2", "param0 line 3"));
      assertThat(readParam1.comments).isEqualTo(List.of("param1 line 1", "param1 line 2", "param1 line 3"));

      deleteKey(client, fullKey);
    }
  }

  @Test public void lastModified_updatesOnValueChange() throws InterruptedException {
    ConfTunnelEtcdDef params = createParams("lastModified_updatesOnValueChange");

    String localPath = "some/folder/lastModified_updatesOnValueChange.hotconf";
    String fullKey = key(params, localPath);

    Conf conf = new Conf();
    conf.confComments.add("This is comment for conf");

    ConfParam param0 = new ConfParam();
    param0.name = "param0";
    param0.valueStr = "value0";
    conf.params.add(param0);

    try (Client client = createClient();
         ConfTunnelEtcd confTunnelEtcd = new ConfTunnelEtcd(client, params)) {
      confTunnelEtcd.write(localPath, conf);

      //
      //
      Instant initialLastModified = confTunnelEtcd.lastModified(localPath);
      //
      //

      Thread.sleep(200);

      param0.valueStr = "changed";
      confTunnelEtcd.write(localPath, conf);

      //
      //
      Instant updatedLastModified = confTunnelEtcd.lastModified(localPath);
      //
      //

      assertThat(initialLastModified).isNotNull();
      assertThat(updatedLastModified).isNotNull();
      assertThat(updatedLastModified).isAfter(initialLastModified);

      deleteKey(client, fullKey);
    }
  }

  @Test public void write_usesConfiguredKeyPrefix() {
    ConfTunnelEtcdDef params = createParams("write_usesConfiguredKeyPrefix");
    params.keyPrefix = "/custom-prefix/";

    String localPath = "some/folder/write_usesConfiguredKeyPrefix.hotconf";
    String customKey = key(params, localPath);
    String defaultKey = KEY_PREFIX + localPath;

    Conf conf = new Conf();
    conf.params.add(new ConfParam());
    conf.params.get(0).name = "param0";
    conf.params.get(0).valueStr = "value0";

    try (Client client = createClient();
         ConfTunnelEtcd confTunnelEtcd = new ConfTunnelEtcd(client, params)) {
      confTunnelEtcd.write(localPath, conf);

      assertThat(keyExists(client, customKey)).isTrue();
      assertThat(keyExists(client, defaultKey)).isFalse();

      deleteKey(client, customKey);
    }
  }

  @Test public void constructor_usesExternalClient() {
    ConfTunnelEtcdDef params = createParams("constructor_usesExternalClient");

    try (Client client = createClient();
         ConfTunnelEtcd confTunnelEtcd = new ConfTunnelEtcd(client, params)) {
      assertThat(confTunnelEtcd).isNotNull();
    }
  }
}
