/** @type {import('next').NextConfig} */
const nextConfig = {
  transpilePackages: ['antd', '@ant-design/icons', '@ant-design/cssinjs'],
  async rewrites() {
    const backend = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
    return [
      {
        source: '/iam/api/:path*',
        destination: `${backend}/iam/api/:path*`,
      },
      {
        source: '/oms/api/:path*',
        destination: `${backend}/oms/api/:path*`,
      },
      {
        source: '/pdm/api/:path*',
        destination: `${backend}/pdm/api/:path*`,
      },
      {
        source: '/som/api/:path*',
        destination: `${backend}/som/api/:path*`,
      },
      {
        source: '/ads/api/:path*',
        destination: `${backend}/ads/api/:path*`,
      },
      {
        source: '/scm/api/:path*',
        destination: `${backend}/scm/api/:path*`,
      },
      {
        source: '/wms/api/:path*',
        destination: `${backend}/wms/api/:path*`,
      },
      {
        source: '/fba/api/:path*',
        destination: `${backend}/fba/api/:path*`,
      },
      {
        source: '/tms/api/:path*',
        destination: `${backend}/tms/api/:path*`,
      },
      {
        source: '/crm/api/:path*',
        destination: `${backend}/crm/api/:path*`,
      },
      {
        source: '/fms/api/:path*',
        destination: `${backend}/fms/api/:path*`,
      },
      {
        source: '/bi/api/:path*',
        destination: `${backend}/bi/api/:path*`,
      },
      {
        source: '/sys/api/:path*',
        destination: `${backend}/sys/api/:path*`,
      },
      {
        source: '/dashboard/api/:path*',
        destination: `${backend}/dashboard/api/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
