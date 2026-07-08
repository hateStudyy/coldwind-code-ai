<template>
  <div id="userRegisterPage">
    <h2 class="title">{{ t('auth.registerTitle') }}</h2>
    <div class="desc">{{ t('auth.slogan') }}</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: t('auth.accountRequired') }]">
        <a-input v-model:value="formState.userAccount" :placeholder="t('auth.accountPlaceholder')" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: t('auth.passwordRequired') },
          { min: 8, message: t('auth.passwordMin') },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" :placeholder="t('auth.passwordPlaceholder')" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: t('auth.confirmPasswordRequired') },
          { min: 8, message: t('auth.passwordMin') },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" :placeholder="t('auth.confirmPasswordPlaceholder')" />
      </a-form-item>
      <div class="tips">
        {{ t('auth.hasAccount') }}
        <RouterLink to="/user/login">{{ t('auth.goLogin') }}</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">{{ t('auth.goRegister') }}</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { useI18n } from '@/i18n'

const router = useRouter()
const { t } = useI18n()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error(t('auth.passwordMismatch')))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success(t('auth.registerSuccess'))
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error(t('auth.registerFailed') + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  background: white;
  max-width: 720px;
  padding: 24px;
  margin: 24px auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
