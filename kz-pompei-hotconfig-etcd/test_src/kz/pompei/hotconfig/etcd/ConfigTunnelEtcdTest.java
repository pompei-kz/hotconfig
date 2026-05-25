package kz.pompei.hotconfig.etcd;

import io.etcd.jetcd.Client;
import java.nio.charset.StandardCharsets;
import java.util.List;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import kz.pompei.hotconfig.etcd.tst_utils.EtcdTestParent;
import org.testng.annotations.Test;
import utils.RND;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigTunnelEtcdTest extends EtcdTestParent {

  @Test public void read_keyDoesNotExists() {
    ConfigTunnelEtcdBuilder builder = createBuilder("read_keyDoesNotExists");

    String localPath = "some/folder/" + RND.str(10) + "/" + RND.str(10) + "/read_keyDoesNotExists.hotconf";
    String fullKey   = key(builder, localPath);

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
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
    ConfigTunnelEtcdBuilder builder = createBuilder("write__multiline_comments");

    String localPath = "some/folder/" + RND.str(10) + "/write__multiline_comments.hotconf";
    String fullKey   = key(builder, localPath);

    Conf conf = new Conf();
    conf.confComments.add("config line 1");
    conf.confComments.add("config line 2");
    conf.confComments.add("config line 3");

    ConfParam param0 = new ConfParam();
    param0.comments.add("param0 line 1");
    param0.comments.add("param0 line 2");
    param0.comments.add("param0 line 3");
    param0.name     = "param0";
    param0.valueStr = "value0";
    param0.error    = "param0 failed\npath=C:\\data\\file";
    conf.params.add(param0);

    ConfParam param1 = new ConfParam();
    param1.comments.add("param1 line 1");
    param1.comments.add("param1 line 2");
    param1.comments.add("param1 line 3");
    param1.name     = "param1";
    param1.valueStr = "value1";
    conf.params.add(param1);

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {

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
        "#ERROR param0 failed",
        "#ERROR path=C:\\data\\file",
        "",
        "#param1 line 1",
        "#param1 line 2",
        "#param1 line 3",
        "param1=value1"
      );

      assertThat(stored).isEqualTo(expectedBody);

      Conf readConf = confTunnelEtcd.read(localPath);

      assertThat(readConf).isNotNull();
      assertThat(readConf.confComments).isEqualTo(List.of("config line 1", "config line 2", "config line 3"));

      ConfParam readParam0 = readConf.params.get(0);
      ConfParam readParam1 = readConf.params.get(1);

      assertThat(readParam0.name).isEqualTo("param0");
      assertThat(readParam1.name).isEqualTo("param1");
      assertThat(readParam0.valueStr).isEqualTo("value0");
      assertThat(readParam1.valueStr).isEqualTo("value1");
      assertThat(readParam0.error).isEqualTo("param0 failed\npath=C:\\data\\file");
      assertThat(readParam1.error).isNull();
      assertThat(readParam0.comments).isEqualTo(List.of("param0 line 1", "param0 line 2", "param0 line 3"));
      assertThat(readParam1.comments).isEqualTo(List.of("param1 line 1", "param1 line 2", "param1 line 3"));

      deleteKey(client, fullKey);
      assertThat(keyExists(client, fullKey)).isFalse();
    }
  }

  @Test public void read__multiline_comments() throws Exception {
    ConfigTunnelEtcdBuilder builder = createBuilder("read__multiline_comments");

    String localPath = "some/folder/" + RND.str(10) + "/read__multiline_comments.hotconf";
    String fullKey   = key(builder, localPath);

    String stored = String.join(
      "\n",
      "#config line 1",
      "#config line 2",
      "#config line 3",
      "",
      "#param0 line 1",
      "#param0 line 2",
      "#param0 line 3",
      "param0=value0",
      "#ERROR param0 failed",
      "#ERROR path=C:\\data\\file",
      "",
      "#param1 line 1",
      "#param1 line 2",
      "#param1 line 3",
      "param1=value1"
    );

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
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
      assertThat(readParam0.error).isEqualTo("param0 failed\npath=C:\\data\\file");
      assertThat(readParam1.error).isNull();
      assertThat(readParam0.comments).isEqualTo(List.of("param0 line 1", "param0 line 2", "param0 line 3"));
      assertThat(readParam1.comments).isEqualTo(List.of("param1 line 1", "param1 line 2", "param1 line 3"));

      deleteKey(client, fullKey);
    }
  }

  @Test public void modificationMarker_updatesOnValueChange() throws InterruptedException {
    ConfigTunnelEtcdBuilder builder = createBuilder("lastModified_updatesOnValueChange");

    String localPath = "some/folder/" + RND.str(10) + "/lastModified_updatesOnValueChange.hotconf";
    String fullKey   = key(builder, localPath);

    Conf conf = new Conf();
    conf.confComments.add("This is comment for conf");

    ConfParam param0 = new ConfParam();
    param0.name     = "param0";
    param0.valueStr = "value0";
    conf.params.add(param0);

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
      confTunnelEtcd.write(localPath, conf);

      //
      //
      Long initialLastModified = confTunnelEtcd.modificationMarker(localPath);
      //
      //

      Thread.sleep(200);

      param0.valueStr = "changed";
      confTunnelEtcd.write(localPath, conf);

      //
      //
      Long updatedLastModified = confTunnelEtcd.modificationMarker(localPath);
      //
      //

      assertThat(initialLastModified).isNotNull();
      assertThat(updatedLastModified).isNotNull();
      assertThat(updatedLastModified).isGreaterThan(initialLastModified);

      deleteKey(client, fullKey);
    }
  }

  @Test public void write_usesConfiguredKeyPrefix() {
    ConfigTunnelEtcdBuilder builder = createBuilder("write_usesConfiguredKeyPrefix");
    builder.keyPrefix("/custom-prefix/");

    String localPath  = "some/folder/" + RND.str(10) + "/write_usesConfiguredKeyPrefix.hotconf";
    String customKey  = key(builder, localPath);
    String defaultKey = KEY_PREFIX + localPath;

    Conf conf = new Conf();
    conf.params.add(new ConfParam());
    conf.params.get(0).name     = "param0";
    conf.params.get(0).valueStr = "value0";

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
      confTunnelEtcd.write(localPath, conf);

      assertThat(keyExists(client, customKey)).isTrue();
      assertThat(keyExists(client, defaultKey)).isFalse();

      deleteKey(client, customKey);
    }
  }

  @Test public void readNoticeLines_keyDoesNotExists() {
    ConfigTunnelEtcdBuilder builder = createBuilder("readNoticeLines_keyDoesNotExists");

    String localPath     = "some/folder/" + RND.str(10) + "/readNoticeLines_keyDoesNotExists.hotconf";
    String fullNoticeKey = key(builder, localPath + builder.noticeExtension());

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
      assertThat(keyExists(client, fullNoticeKey)).isFalse();

      //
      //
      List<String> lines = confTunnelEtcd.readNoticeLines(localPath);
      //
      //

      assertThat(lines).isEmpty();
      assertThat(keyExists(client, fullNoticeKey)).isFalse();
    }
  }

  @Test public void writeNoticeLines() throws Exception {
    ConfigTunnelEtcdBuilder builder = createBuilder("writeNoticeLines");

    String localPath     = "some/folder/" + RND.str(10) + "/writeNoticeLines.hotconf";
    String fullKey       = key(builder, localPath);
    String fullNoticeKey = key(builder, localPath + builder.noticeExtension());

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {

      //
      //
      confTunnelEtcd.writeNoticeLines(localPath, List.of("notice line 1", "notice line 2", ""));
      //
      //

      assertThat(keyExists(client, fullKey)).isFalse();
      assertThat(keyExists(client, fullNoticeKey)).isTrue();

      String stored = new String(
        client.getKVClient().get(byteSequence(fullNoticeKey)).get().getKvs().get(0).getValue().getBytes(),
        StandardCharsets.UTF_8
      );
      assertThat(stored).isEqualTo("notice line 1\nnotice line 2\n");
      assertThat(confTunnelEtcd.readNoticeLines(localPath)).isEqualTo(List.of("notice line 1", "notice line 2", ""));

      deleteKey(client, fullNoticeKey);
    }
  }

  @Test public void writeNoticeLines_emptyListDeletesNoticeKey() {
    ConfigTunnelEtcdBuilder builder = createBuilder("writeNoticeLines_emptyListDeletesNoticeKey");

    String localPath     = "some/folder/" + RND.str(10) + "/writeNoticeLines_emptyListDeletesNoticeKey.hotconf";
    String fullNoticeKey = key(builder, localPath + builder.noticeExtension());

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
      confTunnelEtcd.writeNoticeLines(localPath, List.of("notice line"));
      assertThat(keyExists(client, fullNoticeKey)).isTrue();

      //
      //
      confTunnelEtcd.writeNoticeLines(localPath, List.of());
      //
      //

      assertThat(keyExists(client, fullNoticeKey)).isFalse();
      assertThat(confTunnelEtcd.readNoticeLines(localPath)).isEmpty();
    }
  }

  @Test public void constructor_usesExternalClient() {
    ConfigTunnelEtcdBuilder builder = createBuilder("constructor_usesExternalClient");

    try (Client client = createClient();
         ConfigTunnelEtcd confTunnelEtcd = builder.client(client).build()) {
      assertThat(confTunnelEtcd).isNotNull();
    }
  }
}
