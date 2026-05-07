'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function AdsPage() {
  const router = useRouter();
  useEffect(() => { router.replace('/ads/campaigns'); }, [router]);
  return null;
}
