package app;

import java.nio.file.Path;
import kz.pompei.fui.Fui;
import kz.pompei.hotconfig.core.ConfigTunnelFile;
import kz.pompei.hotconfig.core.HotConfigFactory;

public class Main {
  public static void main(String[] args) {

    EnvSrcFake envSrcFake = new EnvSrcFake();

    Fui fui = Fui.builder().rootDir(Path.of("build/test-application")).build();


    ConfigTunnelFile tunnelFile = ConfigTunnelFile.builder().baseDir(Path.of("build/test-config")).build();

    HotConfigFactory hotConfigFactory = HotConfigFactory.builder().tunnel(tunnelFile).envSrc(envSrcFake).build();

    DbAccessConfig conf = hotConfigFactory.createConf(DbAccessConfig.class);


    fui.button("read-" + DbAccessConfig.class.getSimpleName()).click(() -> {
      System.out.println("ZVpiG1D14I :: DbAccessConfig.host = " + conf.host());
      System.out.println("ZVpiG1D14I :: DbAccessConfig.port = " + conf.port());
      System.out.println("ZVpiG1D14I :: DbAccessConfig.username = " + conf.username());
      System.out.println("ZVpiG1D14I :: DbAccessConfig.password = " + conf.password());
    });


    System.out.println("p1dM1zKEn8 :: Application started");
    fui.go();
    System.out.println("ll6URj7gUe :: Application finished");

  }
}
