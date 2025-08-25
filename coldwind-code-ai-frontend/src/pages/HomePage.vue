<script setup lang="ts">
import { healthCheck } from '@/api/healthController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore();

healthCheck().then((res) => {
  console.log(res)
})

</script>

<template>
  <main>
    <br>
    <br>
    <br>
    <br>
    <br>
    {{ JSON.stringify(loginUserStore.loginUser) }}
  </main>
</template>
