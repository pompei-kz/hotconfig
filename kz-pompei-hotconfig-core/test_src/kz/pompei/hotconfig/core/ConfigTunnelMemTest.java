package kz.pompei.hotconfig.core;

import java.util.ArrayList;
import java.util.List;
import kz.pompei.hotconfig.core.model.Conf;
import kz.pompei.hotconfig.core.model.ConfParam;
import lombok.NonNull;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigTunnelMemTest {

  @Test public void readMissingFile() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();

    //
    //
    Conf conf = tunnel.read("missing.conf");
    //
    //

    assertThat(conf).isNull();
  }

  @Test public void writeAndRead() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();
    Conf            conf   = conf();

    //
    //
    tunnel.write("app.conf", conf);
    Conf readConf = tunnel.read("app.conf");
    //
    //

    assertThat(readConf).isEqualTo(conf);
    assertThat(readConf).isNotSameAs(conf);
    assertThat(readConf).isNotNull();
    assertThat(readConf.params.get(0)).isNotSameAs(conf.params.get(0));
  }

  @Test public void write__storesCopy() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();
    Conf            conf   = conf();

    tunnel.write("app.conf", conf);
    conf.confComments.add("changed config comment");
    conf.params.get(0).comments.add("changed parameter comment");
    conf.params.get(0).valueStr = "changed";
    conf.params.get(0).error    = "changed error";

    //
    //
    Conf readConf = tunnel.read("app.conf");
    //
    //

    assertThat(readConf).isNotNull();
    assertThat(readConf.confComments).containsExactly("configuration comment");
    assertThat(readConf.params).hasSize(1);
    assertThat(readConf.params.get(0).comments).containsExactly("parameter comment");
    assertThat(readConf.params.get(0).name).isEqualTo("host");
    assertThat(readConf.params.get(0).valueStr).isEqualTo("localhost");
    assertThat(readConf.params.get(0).error).isEqualTo("host is unavailable");
  }

  @Test public void read__returnsCopy() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();
    tunnel.write("app.conf", conf());

    Conf readConf = tunnel.read("app.conf");
    assertThat(readConf).isNotNull();
    readConf.confComments.add("changed config comment");
    readConf.params.get(0).comments.add("changed parameter comment");
    readConf.params.get(0).valueStr = "changed";
    readConf.params.get(0).error    = "changed error";

    //
    //
    Conf secondReadConf = tunnel.read("app.conf");
    //
    //

    assertThat(secondReadConf).isNotNull();
    assertThat(secondReadConf.confComments).containsExactly("configuration comment");
    assertThat(secondReadConf.params).hasSize(1);
    assertThat(secondReadConf.params.get(0).comments).containsExactly("parameter comment");
    assertThat(secondReadConf.params.get(0).name).isEqualTo("host");
    assertThat(secondReadConf.params.get(0).valueStr).isEqualTo("localhost");
    assertThat(secondReadConf.params.get(0).error).isEqualTo("host is unavailable");
  }

  @Test public void modificationMarker() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();

    assertThat(tunnel.modificationMarker("app.conf")).isNull();

    tunnel.write("app.conf", conf());
    Long firstMarker = tunnel.modificationMarker("app.conf");

    tunnel.write("app.conf", conf());
    Long secondMarker = tunnel.modificationMarker("app.conf");

    assertThat(firstMarker).isNotNull();
    assertThat(secondMarker).isNotNull();
    assertThat(secondMarker).isGreaterThan(firstMarker);
  }

  @Test public void readNoticeLines_missingFile() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();

    //
    //
    List<String> lines = tunnel.readNoticeLines("missing.conf");
    //
    //

    assertThat(lines).isEmpty();
  }

  @Test public void writeNoticeLines() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();
    List<String>    lines  = new ArrayList<>(List.of("notice line 1", "notice line 2", ""));

    //
    //
    tunnel.writeNoticeLines("app.conf", lines);
    //
    //

    lines.add("changed");

    assertThat(tunnel.readNoticeLines("app.conf")).containsExactly("notice line 1", "notice line 2", "");
    assertThatThrownBy(() -> tunnel.readNoticeLines("app.conf").add("changed"))
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test public void storageIsSeparatedByLocalPath() {

    ConfigTunnelMem tunnel = new ConfigTunnelMem();
    Conf            app    = conf("host", "localhost");
    Conf            nested = conf("port", "5432");

    tunnel.write("app.conf", app);
    tunnel.write("nested/app.conf", nested);
    tunnel.writeNoticeLines("app.conf", List.of("app notice"));
    tunnel.writeNoticeLines("nested/app.conf", List.of("nested notice"));

    //
    //
    Conf readApp    = tunnel.read("app.conf");
    Conf readNested = tunnel.read("nested/app.conf");
    //
    //

    assertThat(readApp).isEqualTo(app);
    assertThat(readNested).isEqualTo(nested);
    assertThat(tunnel.readNoticeLines("app.conf")).containsExactly("app notice");
    assertThat(tunnel.readNoticeLines("nested/app.conf")).containsExactly("nested notice");
  }

  private @NonNull Conf conf() {
    Conf conf = conf("host", "localhost");
    conf.confComments.add("configuration comment");
    conf.params.get(0).comments.add("parameter comment");
    conf.params.get(0).error = "host is unavailable";
    return conf;
  }

  private @NonNull Conf conf(String name, String valueStr) {
    Conf      conf  = new Conf();
    ConfParam param = new ConfParam();
    param.name     = name;
    param.valueStr = valueStr;
    conf.params.add(param);
    return conf;
  }
}
