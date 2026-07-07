<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式优化 */
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
}

html {
  overflow-x: hidden;
}

/* 轻量主题：主色橙色，不大改，仅覆盖关键色 */
:root {
  --brand-primary: #fa8c16;
}

/* Ant Design Vue 主要组件色彩微调 */
.ant-btn-primary,
.ant-tag-has-color,
.ant-pagination-item-active,
.ant-switch-checked,
.ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled),
.ant-progress-bg {
  background-color: var(--brand-primary) !important;
  border-color: var(--brand-primary) !important;
}

.ant-btn-primary:hover,
.ant-btn-primary:focus {
  background-color: #ff9c2a !important;
  border-color: #ff9c2a !important;
}

/* Header 标题色呼应主色 */
.site-title {
  color: var(--brand-primary) !important;
}
</style>
