# UI and Navigation

## Navigation structure
Bottom navigation with four destinations:
- dashboard
- trends
- advice
- settings

## Dashboard
Purpose:
- show today's step count
- show today's average heart rate
- show today's sleep duration
- show a simple status summary

## Trends
Purpose:
- show recent 7-day trends
- v0.1 renders list sections instead of charts

Sections:
- recent 7-day steps
- recent 7-day average heart rate
- recent 7-day sleep duration

## Advice
Purpose:
- import mock data
- generate today's suggestions
- render fused suggestion cards

Each suggestion card shows:
- title
- content
- explanation
- source agents

## Settings
Purpose:
- placeholder for future settings such as:
  - agent switches
  - refresh interval
  - privacy information
  - data source management

## UX assumptions in v0.1
- one demo user id: `demo_user`
- import action may be triggered from Advice page
- dashboard and trends will look empty before data import
- advice generation uses today's data only
