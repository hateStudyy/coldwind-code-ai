package in.yumi.coldwindcodeai.service.impl;

import in.yumi.coldwindcodeai.exception.BusinessException;
import in.yumi.coldwindcodeai.exception.ErrorCode;
import in.yumi.coldwindcodeai.model.entity.App;
import in.yumi.coldwindcodeai.model.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

class AppServiceImplDeployPermissionTest {

    @Test
    void deployAppShouldRejectNonOwner() {
        Long appId = 1001L;
        App app = new App();
        app.setId(appId);
        app.setUserId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        TestableAppServiceImpl appService = new TestableAppServiceImpl(app);

        BusinessException exception = Assertions.assertThrows(
                BusinessException.class,
                () -> appService.deployApp(appId, otherUser)
        );
        Assertions.assertEquals(ErrorCode.NO_AUTH_ERROR.getCode(), exception.getCode());
    }

    private static class TestableAppServiceImpl extends AppServiceImpl {

        private final App app;

        private TestableAppServiceImpl(App app) {
            this.app = app;
        }

        @Override
        public App getById(Serializable id) {
            return app;
        }
    }
}
