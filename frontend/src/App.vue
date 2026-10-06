<template>
  <a-config-provider :theme="themeConfig" :locale="zhCN">
    <router-view v-slot="{ Component }">
      <transition name="app" mode="out-in">
        <component :is="Component" />
      </transition>
    </router-view>
  </a-config-provider>
</template>

<script setup>
import { reactive } from 'vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'

/**
 * ICBC 中国工商银行 - 阿里巴巴设计规范 v2
 * 纯色克制、专业金融风
 */
const themeConfig = reactive({
  token: {
    colorPrimary: '#C8102E',
    colorSuccess: '#52C41A',
    colorWarning: '#FAAD14',
    colorError: '#FF4D4F',
    colorInfo: '#C8102E',

    colorTextBase: '#1A1A1A',
    colorTextSecondary: '#666666',
    colorTextTertiary: '#999999',

    colorBgBase: '#FFFFFF',
    colorBgContainer: '#FFFFFF',
    colorBgLayout: '#F5F6FA',

    colorBorder: '#EBEEF5',
    colorBorderSecondary: '#F0F0F0',

    borderRadius: 4,
    borderRadiusLG: 8,

    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "PingFang SC", "Microsoft YaHei", sans-serif',
    fontSize: 14,
    fontSizeLG: 16,
    fontSizeXL: 20,

    padding: 16,
    paddingLG: 24,
    margin: 16,
    marginLG: 24,

    boxShadow: '0 1px 3px rgba(0, 0, 0, 0.06)',
    boxShadowSecondary: '0 2px 8px rgba(0, 0, 0, 0.08)',

    colorLink: '#C8102E',
    colorLinkHover: '#9B0023',
  },
  components: {
    Layout: {
      headerBg: '#FFFFFF',
      headerHeight: 56,
      headerPadding: '0 24px',
      siderBg: '#FFFFFF',
      bodyBg: '#F5F6FA',
    },
    Menu: {
      itemBg: 'transparent',
      itemColor: '#595959',
      itemHoverColor: '#C8102E',
      itemHoverBg: '#FFF1F0',
      itemSelectedColor: '#C8102E',
      itemSelectedBg: '#FFF1F0',
      itemActiveBg: '#FFE7E5',
      iconSize: 18,
      collapsedIconSize: 20,
      itemHeight: 40,
      itemMarginInline: 8,
      itemBorderRadius: 6,
    },
    Button: {
      primaryColor: '#FFFFFF',
      primaryShadow: '0 1px 3px rgba(200, 16, 46, 0.2)',
      defaultBg: '#FFFFFF',
      defaultBorderColor: '#DCDFE6',
      defaultHoverBorderColor: '#C8102E',
      defaultHoverColor: '#C8102E',
      borderRadius: 4,
      borderRadiusLG: 6,
      controlHeight: 36,
      controlHeightLG: 44,
      fontWeight: 500,
    },
    Card: {
      borderRadius: 8,
      borderRadiusLG: 12,
      padding: 20,
      paddingLG: 24,
      colorBorderSecondary: '#EBEEF5',
    },
    Table: {
      headerBg: '#FAFAFA',
      headerColor: '#1A1A1A',
      headerBorderRadius: 6,
      rowHoverBg: '#FFF1F0',
      borderColor: '#EBEEF5',
      cellPaddingBlock: 12,
      cellPaddingInline: 16,
    },
    Input: {
      borderRadius: 4,
      borderRadiusLG: 6,
      controlHeight: 36,
      controlHeightLG: 44,
      hoverBorderColor: '#C8102E',
      activeBorderColor: '#C8102E',
    },
    Tag: {
      borderRadius: 4,
      defaultBg: '#F5F5F5',
      defaultColor: '#666666',
    },
    Tabs: {
      inkBarColor: '#C8102E',
      itemActiveColor: '#C8102E',
      itemHoverColor: '#C8102E',
    },
    Modal: {
      borderRadiusLG: 12,
      titleFontSize: 18,
      paddingLG: 24,
    },
  },
})
</script>

<style>
.app-enter-active,
.app-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.app-enter-from { opacity: 0; transform: translateY(6px); }
.app-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
